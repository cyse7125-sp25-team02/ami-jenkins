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

def folder = instance.createProject(Folder.class, "infra-jenkins")
def multibranchJob = folder.createProject(WorkflowMultiBranchProject.class, "terraform-validation")

def githubSource = new GitHubSCMSource(
    githubOrg,
    githubRepo
)
githubSource.setCredentialsId(githubCredentialsId)

List<SCMSourceTrait> traits = [
    new BranchDiscoveryTrait(1),
    new OriginPullRequestDiscoveryTrait(1),
    new ForkPullRequestDiscoveryTrait(1, new ForkPullRequestDiscoveryTrait.TrustPermission())
]
githubSource.setTraits(traits)

def branchSource = new BranchSource(githubSource)
multibranchJob.getSourcesList().add(branchSource)

multibranchJob.save()
multibranchJob.scheduleBuild()
