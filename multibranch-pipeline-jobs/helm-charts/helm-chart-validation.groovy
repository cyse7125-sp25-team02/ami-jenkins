folder('helm-charts')

multibranchPipelineJob('helm-charts/helm-chart-validation') {
    branchSources {
        github {
            id('cyse7125-sp25-team02')
            repoOwner('cyse7125-sp25-team02')
            repository('helm-charts')
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
            contextLabel('helm-chart-validation')
            typeSuffix(false)
            multipleStatuses(false)
        }
    }
    
    factory {
        workflowBranchProjectFactory {
            scriptPath('jenkinsfiles/Jenkinsfile.helm-chart-validation')
        }
    }
}
