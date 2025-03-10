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

// Create multibranch pipeline job for terraform-validation
List<SCMSourceTrait> terraformTraits = [
    new ForkPullRequestDiscoveryTrait(1, new ForkPullRequestDiscoveryTrait.TrustPermission())
]

def githubSource = new GitHubSCMSource(
    "cyse7125-sp25-team02",
    "infra-jenkins"
)
githubSource.setCredentialsId("github-credentials")
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
