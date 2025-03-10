import jenkins.model.*
import hudson.model.*
import jenkins.model.Jenkins
import hudson.PluginManager
import hudson.model.UpdateCenter
import hudson.model.UpdateSite

def plugins = [
    'github-branch-source',
    'workflow-aggregator',
    'git',
    'credentials-binding',
    'pipeline-utility-steps',
    'github-pullrequest',
    'timestamper',
    'ws-cleanup',
    'email-ext',
    'terraform',
    'credentials',
    'github',
    'generic-webhook-trigger',
    'docker-workflow',
    'docker-commons',
    'job-dsl',
    'configuration-as-code',
    'multibranch-scan-webhook-trigger'
]

def instance = Jenkins.getInstance()
def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()

plugins.each { plugin ->
    def installedPlugin = pm.getPlugin(plugin)
    
    if (installedPlugin) {
        println("✅ Plugin '${plugin}' is already installed. Skipping installation.")
    } else {
        println("📥 Installing plugin: '${plugin}'")
        def pluginInstall = uc.getPlugin(plugin)
        
        if (pluginInstall) {
            def future = pluginInstall.deploy()
            while (!future.isDone()) { Thread.sleep(500) }
        } else {
            println("❌ Plugin '${plugin}' not found in Update Center. Skipping...")
        }
    }
}

// Save the Jenkins instance configuration
instance.save()
println("✅ Plugins installation process completed successfully.")
