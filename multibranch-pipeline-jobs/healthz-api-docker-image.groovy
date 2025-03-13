multibranchPipelineJob('healthz-api-docker-image') {
    branchSources {
        github {
            id('healthz-api-docker-image')
            repoOwner('cyse7125-sp25-team02')
            repository('webapp-hello-world')

            checkoutCredentialsId('github-credentials')
            scanCredentialsId('github-credentials')

            buildOriginBranch(true)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(false)
            buildForkPRHead(false)
            buildForkPRMerge(false)
        }
    }
}
