folder('healthz-database-migration')

multibranchPipelineJob('healthz-database-migration/healthz-db-migration-docker-image') {
    branchSources {
        github {
            id('healthz-db-migration-docker-image')
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
