#!groovy

import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.*
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.util.Secret
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.*
import org.jenkinsci.plugins.github_branch_source.*
import com.coravy.hudson.plugins.github.GithubProjectProperty
import java.util.Properties
import hudson.model.StringParameterDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.*

Properties props = new Properties()
File envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

String githubCredentialsId = props.getProperty('GITHUB_CREDENTIALS_ID')
String githubUsername = props.getProperty('GITHUB_USERNAME')
String githubTokenId = props.getProperty('GITHUB_TOKEN_ID')
String githubToken = props.getProperty('GITHUB_TOKEN')
String githubRepoUrl = props.getProperty('GITHUB_REPO_URL')
String githubOrg = props.getProperty('GITHUB_ORG')
String githubRepo = props.getProperty('GITHUB_REPO')

def instance = Jenkins.instance
def domain = Domain.global()
def store = instance.getExtensionList("com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()

def tokenCred = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    githubTokenId,
    "GitHub Token",
    Secret.fromString(githubToken)
)
store.addCredentials(domain, tokenCred)

def githubCred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    githubCredentialsId,
    "GitHub Credentials",
    githubUsername,
    githubToken
)
store.addCredentials(domain, githubCred)

def jobName = 'Terraform PR Validation'

def pipelineScript = '''
node() {
    properties([
        pipelineTriggers([
            [
                $class: 'GenericTrigger',
                genericVariables: [
                    [key: 'action', value: '$.action'],
                    [key: 'pull_request_number', value: '$.pull_request.number'],
                    [key: 'pull_request_sha', value: '$.pull_request.head.sha']
                ],
                causeString: 'PR $pull_request_number $action',
                token: "''' + githubToken + '''",
                regexpFilterText: '$action',
                regexpFilterExpression: '(opened|reopened|synchronize)'
            ]
        ])
    ])
    
    timeout(time: 15, unit: 'MINUTES') {
        try {
            stage('Checkout') {
                script {
                    def isPR = env.pull_request_number?.trim()
                    
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: isPR ? "origin/pr/${pull_request_number}/head" : 'master']],
                        extensions: [[$class: 'CleanBeforeCheckout']],
                        userRemoteConfigs: [[
                            url: "''' + githubRepoUrl + '''",
                            credentialsId: "''' + githubCredentialsId + '''",
                            refspec: isPR ? '+refs/pull/*/head:refs/remotes/origin/pr/*/head' : '+refs/heads/*:refs/remotes/origin/*'
                        ]]
                    ])
                }
            }
            
            stage('Terraform Init') {
                withEnv(['TF_IN_AUTOMATION=true', 'TF_INPUT=false']) {
                    sh 'terraform init -no-color'
                }
            }
            
            stage('Terraform Format Check') {
                withEnv(['TF_IN_AUTOMATION=true', 'TF_INPUT=false']) {
                    sh 'terraform fmt -check -no-color'
                }
            }
            
            stage('Terraform Validate') {
                withEnv(['TF_IN_AUTOMATION=true', 'TF_INPUT=false']) {
                    sh 'terraform validate -no-color'
                }
            }
        } catch (Exception e) {
            currentBuild.result = 'FAILURE'
            throw e
        } finally {
            script {
                if (env.pull_request_number) {
                    def status = currentBuild.result ?: 'SUCCESS'
                    def statusEmoji = status == 'SUCCESS' ? '✅' : '❌'
                    
                    withCredentials([string(credentialsId: "''' + githubTokenId + '''", variable: 'GITHUB_TOKEN')]) {
                        try {
                            def commentBody = """**Terraform Validation Results**
- Status: ${statusEmoji} ${status}
- Build Number: #${env.BUILD_NUMBER}
- Console Log: ${env.BUILD_URL}"""

                            def jsonBody = groovy.json.JsonOutput.toJson([body: commentBody])
                            writeFile file: 'comment.json', text: jsonBody
                            
                            sh """#!/bin/bash
                                set -e
                                curl -sS -X POST \\
                                    -H 'Authorization: token '\$GITHUB_TOKEN \\
                                    -H 'Accept: application/vnd.github.v3+json' \\
                                    'https://api.github.com/repos/''' + githubOrg + '''/''' + githubRepo + '''/issues/'${env.pull_request_number}'/comments' \\
                                    -d @comment.json
                            """
                        } catch (Exception e) {
                            echo "Failed to post GitHub comment: ${e.getMessage()}"
                        } finally {
                            sh 'rm -f comment.json || true'
                        }
                    }
                } else {
                    echo "Skipping GitHub comment - not a PR build"
                }
            }
            deleteDir()
        }
    }
}
'''

def pipelineJob = new WorkflowJob(instance, jobName)
pipelineJob.definition = new CpsFlowDefinition(pipelineScript, true)
instance.reload()

if (Jenkins.instance.getItem(jobName)) {
    Jenkins.instance.getItem(jobName).scheduleBuild2(0)
    println "Triggered job: ${jobName}"
}
