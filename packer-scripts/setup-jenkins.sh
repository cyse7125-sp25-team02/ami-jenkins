#!/bin/bash

# Wait for Jenkins to be installed
sudo systemctl stop jenkins

# Create Jenkins init script to disable setup wizard
sudo mkdir -p /var/lib/jenkins/init.groovy.d
sudo bash -c "cat > /var/lib/jenkins/init.groovy.d/basic-security.groovy << EOL
#!groovy
import jenkins.model.*
import hudson.security.*
import jenkins.install.InstallState

def instance = Jenkins.getInstance()
instance.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
instance.setSecurityRealm(hudsonRealm)

def user = hudsonRealm.createAccount('${JENKINS_ADMIN_USER}', '${JENKINS_ADMIN_PASSWORD}')
user.save()

instance.save()
EOL"

# Set environment variables
export CASC_JENKINS_CONFIG=/var/lib/jenkins/jenkins.yaml
export JENKINS_HOME=/var/lib/jenkins

# Modify Jenkins default configuration
sudo bash -c 'cat > /etc/default/jenkins << EOL
JENKINS_HOME=/var/lib/jenkins
JENKINS_ARGS="--webroot=/var/cache/jenkins/war --httpPort=8080"
JAVA_ARGS="-Djenkins.install.runSetupWizard=false -Dcasc.jenkins.config=/var/lib/jenkins/jenkins.yaml -Djenkins.security.ApiTokenProperty.adminCanGenerateNewTokens=true"
CASC_JENKINS_CONFIG=/var/lib/jenkins/jenkins.yaml
EOL'

# Install plugins from plugins.yaml
sudo bash -c "cat > /var/lib/jenkins/init.groovy.d/install-plugins.groovy << EOL
#!groovy
import jenkins.model.*

def pluginsToInstall = [
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
    'docker-commons'
]

def instance = Jenkins.get()
def pm = instance.pluginManager
def uc = instance.updateCenter
uc.updateAllSites()

pluginsToInstall.each { pluginName ->
    if (!pm.getPlugin(pluginName)) {
        def plugin = uc.getPlugin(pluginName)
        if (plugin) {
            def future = plugin.deploy()
            while (!future.isDone()) { Thread.sleep(500) }
        }
    }
}
EOL"

sudo tee /etc/jenkins.env > /dev/null <<EOF
JENKINS_ADMIN_USER=${JENKINS_ADMIN_USER}
JENKINS_ADMIN_PASSWORD=${JENKINS_ADMIN_PASSWORD}
JENKINS_URL=${JENKINS_URL}
GITHUBB_CREDENTIALS_ID=${GITHUBB_CREDENTIALS_ID}
GITHUBB_USERNAME=${GITHUBB_USERNAME}
GITHUBB_TOKEN_ID=${GITHUBB_TOKEN_ID}
GITHUBB_TOKEN=${GITHUBB_TOKEN}
GITHUBB_REPO_URL=${GITHUBB_REPO_URL}
GITHUBB_ORG=${GITHUBB_ORG}
GITHUBB_REPO=${GITHUBB_REPO}
STATIC_SITE_REPO=${STATIC_SITE_REPO}
DOCKER_USERNAME=${DOCKER_USERNAME}
DOCKER_TOKEN=${DOCKER_TOKEN}
DOCKER_IMAGE=${DOCKER_IMAGE}
EOF

sudo mv /tmp/z-create-pipeline-job.groovy /var/lib/jenkins/init.groovy.d/

# Set correct permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo chmod -R 755 /var/lib/jenkins

sudo chown jenkins:jenkins /etc/jenkins.env
sudo chmod 600 /etc/jenkins.env

# Restart Jenkins to apply changes
sudo systemctl restart jenkins

# Wait for Jenkins to start
sleep 60
