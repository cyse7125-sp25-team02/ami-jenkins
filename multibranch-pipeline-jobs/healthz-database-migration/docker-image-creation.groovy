folder('healthz-database-migration')

multibranchPipelineJob('healthz-database-migration/docker-image-creation') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('healthz-database-migration')

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
