folder('db-backup-operator')

multibranchPipelineJob('db-backup-operator/docker-image-creation') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('db-backup-operator')
            checkoutCredentialsId('github-credentials')
            scanCredentialsId('github-credentials')
        }
    }

    configure { node ->
        def traits = node / sources / data / 'jenkins.branch.BranchSource' / source / traits
        
        traits << 'org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait' {
            strategyId(1)
        }
        
        traits << 'org.jenkinsci.plugins.githubScmTraitNotificationContext.NotificationContextTrait' {
            contextLabel('docker-image-creation')
            typeSuffix(false)
            multipleStatuses(false)
        }
    }

    factory {
        workflowBranchProjectFactory {
            scriptPath('jenkinsfiles/Jenkinsfile.docker-image-creation')
        }
    }
}
