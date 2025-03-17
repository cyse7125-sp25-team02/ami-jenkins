folder('infra-jenkins')

multibranchPipelineJob('infra-jenkins/terraform-validation') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('infra-jenkins')

            checkoutCredentialsId('github-credentials')
            scanCredentialsId('github-credentials')

            buildOriginBranch(false)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(false)
            buildForkPRHead(true)
            buildForkPRMerge(false)
        }
    }

    factory {
        workflowBranchProjectFactory {
            scriptPath('jenkinsfiles/Jenkinsfile.terraform-validation')
        }
    }
}
