folder('api-server')
multibranchPipelineJob('api-server/conventional-commit-check') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('api-server')
            checkoutCredentialsId('github-credentials')
            scanCredentialsId('github-credentials')
        }
    }
    
    configure { node ->
        def traits = node / sources / data / 'jenkins.branch.BranchSource' / source / traits
        
        traits << 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait' {
            strategyId(2)
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
        }
        
        traits << 'org.jenkinsci.plugins.githubScmTraitNotificationContext.NotificationContextTrait' {
            contextLabel('conventional-commit-check')
            typeSuffix(false)
            multipleStatuses(false)
        }
    }
    
    factory {
        workflowBranchProjectFactory {
            scriptPath('jenkinsfiles/Jenkinsfile.conventional-commit-check')
        }
    }
}
