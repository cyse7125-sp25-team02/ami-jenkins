
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
