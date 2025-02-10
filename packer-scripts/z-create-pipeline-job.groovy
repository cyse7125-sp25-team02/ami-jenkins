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

def props = new Properties()
def envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

def githubCredentialsId = props.getProperty('GITHUB_CREDENTIALS_ID')
def githubUsername = props.getProperty('GITHUB_USERNAME')
def githubTokenId = props.getProperty('GITHUB_TOKEN_ID')
def githubToken = props.getProperty('GITHUB_TOKEN')
def githubRepoUrl = props.getProperty('GITHUB_REPO_URL')
def githubOrg = props.getProperty('GITHUB_ORG')
def githubRepo = props.getProperty('GITHUB_REPO')

// Jenkins instance setup
def instance = Jenkins.instance
def domain = Domain.global()
def store = instance.getExtensionList("com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()

// Create credentials
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

def pipelineScript = """
node('any') {
    properties([
        pipelineTriggers([
            [\$class: 'GitHubPullRequestTrigger', 
            triggerOnPullRequest: true, 
            branchRestriction: 'master', 
            useGitHubHooks: true]
        ])
    ])
    
    timeout(time: 15, unit: 'MINUTES') {
        try {
            stage('Checkout') {
                checkout([
                    \$class: 'GitSCM',
                    branches: [[name: env.CHANGE_BRANCH]],
                    extensions: [],
                    userRemoteConfigs: [[
                        url: ${githubRepoUrl},
                        credentialsId: ${githubCredentialsId}
                    ]]
                ])
            }
            
            stage('Terraform Init') {
                sh 'terraform init'
            }
            
            stage('Terraform Format Check') {
                sh 'terraform fmt -check'
            }
            
            stage('Terraform Validate') {
                sh 'terraform validate'
            }
        } finally {
            deleteDir()
            
            if (env.CHANGE_ID) {
                def statusMessage = '''
                **Terraform Validation Results**
                - Status: \${currentBuild.result == 'SUCCESS' ? '✅ Success' : '❌ Failure'}
                - Build Number: #\${env.BUILD_NUMBER}
                - Console Log: \${env.RUN_DISPLAY_URL}
                '''
                
                withCredentials([string(credentialsId: ${githubTokenId}, variable: 'GITHUB_TOKEN')]) {
                    sh '''
                    curl -sS -X POST \\
                        -H "Authorization: token \$GITHUB_TOKEN" \\
                        -H "Accept: application/vnd.github.v3+json" \\
                        "https://api.github.com/repos/${githubOrg}/${githubRepo}/issues/\${env.CHANGE_ID}/comments" \\
                        -d "{\\"body\\": \\"\${statusMessage.replace('"', '\\\\"')}\\"}"
                    '''
                }
            }
        }
    }
}
"""

def job = new WorkflowJob(instance, jobName)
job.definition = new CpsFlowDefinition(pipelineScript, true)
instance.reload()
