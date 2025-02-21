import jenkins.model.*
import org.jenkinsci.plugins.workflow.multibranch.*
import jenkins.branch.*
import jenkins.plugins.git.*
import org.jenkinsci.plugins.github_branch_source.*
import com.cloudbees.hudson.plugins.folder.*
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.util.Secret
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.*
import jenkins.scm.api.trait.SCMSourceTrait
import jenkins.branch.DefaultBranchPropertyStrategy
import jenkins.branch.BranchSource
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait


// envs
Properties props = new Properties()
File envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

String jenkinsUrl = props.getProperty('JENKINS_URL')
String githubCredentialsId = props.getProperty('GITHUBB_CREDENTIALS_ID')
String githubUsername = props.getProperty('GITHUBB_USERNAME')
String githubTokenId = props.getProperty('GITHUBB_TOKEN_ID')
String githubToken = props.getProperty('GITHUBB_TOKEN')
String githubOrg = props.getProperty('GITHUBB_ORG')
String infraJenkinsRepo = props.getProperty('INFRA_JENKINS_REPO')
String staticSiteRepo = props.getProperty('STATIC_SITE_REPO')
String tfGCPInfraRepo = props.getProperty('TF_GCP_INFRA_REPO')
String dockerUsername = props.getProperty('DOCKER_USERNAME')
String dockerToken = props.getProperty('DOCKER_TOKEN')
String dockerImage = props.getProperty('DOCKER_IMAGE')


// Set Jenkins URL
JenkinsLocationConfiguration location = JenkinsLocationConfiguration.get()
location.setUrl(jenkinsUrl)
location.save()


// Create GitHub credentials
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

def dockerCred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "dockerhub-credentials",
    "Docker Hub Credentials",
    dockerUsername,
    dockerToken
)
store.addCredentials(domain, dockerCred)

def dockerImageName = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "docker-image",
    "Docker Image",
    Secret.fromString(dockerImage)
)
store.addCredentials(domain, dockerImageName)


// Create multibranch pipeline job for terraform-validation
List<SCMSourceTrait> terraformTraits = [
    new ForkPullRequestDiscoveryTrait(1, new ForkPullRequestDiscoveryTrait.TrustPermission())
]

def githubSource = new GitHubSCMSource(
    githubOrg,
    infraJenkinsRepo
)
githubSource.setCredentialsId(githubCredentialsId)
githubSource.setApiUri("https://api.github.com")
githubSource.setTraits(terraformTraits)

def folder = instance.createProject(Folder.class, "infra-jenkins")
def multibranchJob = folder.createProject(WorkflowMultiBranchProject.class, "terraform-validation")
def branchSource = new BranchSource(githubSource)
multibranchJob.getSourcesList().add(branchSource)
multibranchJob.save()


// Multibranch pipeline for Terraform GCP infra
List<SCMSourceTrait> tfGCPInfraTraits = [
    new ForkPullRequestDiscoveryTrait(1, new ForkPullRequestDiscoveryTrait.TrustPermission())
]

def githubSource1 = new GitHubSCMSource(
    githubOrg,
    tfGCPInfraRepo
)
githubSource1.setCredentialsId(githubCredentialsId)
githubSource1.setApiUri("https://api.github.com")
githubSource1.setTraits(tfGCPInfraTraits)

def folder1 = instance.createProject(Folder.class, "tf-gcp-infra")
def multibranchJob1 = folder1.createProject(WorkflowMultiBranchProject.class, "terraform-validation")
def branchSource1 = new BranchSource(githubSource1)
multibranchJob1.getSourcesList().add(branchSource1)
multibranchJob1.save()


// Create multibranch pipeline job for docker image creation
List<SCMSourceTrait> dockerTraits = [
    new BranchDiscoveryTrait(1)
]

def githubSource2 = new GitHubSCMSource(
    githubOrg,
    staticSiteRepo
)
githubSource2.setCredentialsId(githubCredentialsId)
githubSource2.setApiUri("https://api.github.com")
githubSource2.setTraits(dockerTraits)

def folder2 = instance.createProject(Folder.class, "static-site")
def multibranchJob2 = folder2.createProject(WorkflowMultiBranchProject.class, "docker-image-creation")
def branchSource2 = new BranchSource(githubSource2)
multibranchJob2.getSourcesList().add(branchSource2)
multibranchJob2.save()
