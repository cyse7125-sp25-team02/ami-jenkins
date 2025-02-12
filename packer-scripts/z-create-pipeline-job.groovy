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
import hudson.model.*
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import java.util.Collections

Properties props = new Properties()
File envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

String githubCredentialsId = props.getProperty('GITHUBB_CREDENTIALS_ID')
String githubUsername = props.getProperty('GITHUBB_USERNAME')
String githubTokenId = props.getProperty('GITHUBB_TOKEN_ID')
String githubToken = props.getProperty('GITHUBB_TOKEN')
String githubRepoUrl = props.getProperty('GITHUBB_REPO_URL')
String githubOrg = props.getProperty('GITHUBB_ORG')
String githubRepo = props.getProperty('GITHUBB_REPO')

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

def pipelineJob = new WorkflowJob(instance, jobName)
def userRemoteConfig = new UserRemoteConfig(githubRepoUrl, "origin", null, githubCredentialsId)
def scmConfig = new GitSCM(
    Collections.singletonList(userRemoteConfig),
    Collections.emptyList(),
    false,
    Collections.emptyList(),
    null,
    null,
    Collections.emptyList()
)

pipelineJob.setDefinition(new CpsScmFlowDefinition(scmConfig, "Jenkinsfile"))
instance.reload()

if (Jenkins.instance.getItem(jobName)) {
    Jenkins.instance.getItem(jobName).scheduleBuild2(0)
    println "Triggered job: ${jobName}"
}
