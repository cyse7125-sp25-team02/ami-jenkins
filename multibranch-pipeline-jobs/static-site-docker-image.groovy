multibranchPipelineJob('static-site-docker-image') {
    branchSources {
        github {
            id('static-site-docker-image')
            repoOwner('cyse7125-sp25-team02')
            repository('static-site')

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
