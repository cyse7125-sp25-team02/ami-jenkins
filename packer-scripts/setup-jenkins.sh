#!/bin/bash

# Wait for Jenkins to be installed
sleep 5

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
EOL'

# Install plugins from plugins.yaml
sudo bash -c "cat > /var/lib/jenkins/init.groovy.d/install-plugins.groovy << EOL
#!groovy
import jenkins.model.*

def pluginsToInstall = [
    'cloudbees-folder',
    'antisamy-markup-formatter',
    'build-timeout',
    'credentials-binding',
    'timestamper',
    'ws-cleanup',
    'ant',
    'gradle',
    'workflow-aggregator',
    'github-branch-source',
    'pipeline-github-lib',
    'pipeline-stage-view',
    'git',
    'ssh-slaves',
    'matrix-auth',
    'pam-auth',
    'ldap',
    'email-ext',
    'mailer',
    'pipeline-utility-steps',
    'terraform',
    'github-pullrequest',
    'job-dsl',
    'ghprb'
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

echo "JENKINS_ADMIN_USER='${JENKINS_ADMIN_USER}'" | sudo tee -a /etc/default/jenkins
echo "JENKINS_ADMIN_PASSWORD='${JENKINS_ADMIN_PASSWORD}'" | sudo tee -a /etc/default/jenkins
echo "JENKINS_URL='${JENKINS_URL}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_CREDENTIALS_ID='${GITHUB_CREDENTIALS_ID}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_USERNAME='${GITHUB_USERNAME}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_TOKEN_ID='${GITHUB_TOKEN_ID}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_TOKEN='${GITHUB_TOKEN}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_REPO_URL='${GITHUB_REPO_URL}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_ORG='${GITHUB_ORG}'" | sudo tee -a /etc/default/jenkins
echo "GITHUB_REPO='${GITHUB_REPO}'" | sudo tee -a /etc/default/jenkins

sudo mv /tmp/create-pipeline-job.groovy /var/lib/jenkins/init.groovy.d/

# Set correct permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo chmod -R 755 /var/lib/jenkins

# Restart Jenkins to apply changes
sudo systemctl restart jenkins

# Wait for Jenkins to start
sleep 5
