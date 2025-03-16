folder('tf-gcp-infra')

multibranchPipelineJob('tf-gcp-infra/terraform-validation') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('tf-gcp-infra')

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
}
