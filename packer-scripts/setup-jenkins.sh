#!/bin/bash

# Wait for Jenkins to be installed
while [ ! -f /usr/share/java/jenkins.war ]; do
    sleep 5
done

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
# sudo jenkins-plugin-cli --plugin-file /var/lib/jenkins/plugins.yaml -d /var/lib/jenkins/plugins

# Set correct permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo chmod -R 755 /var/lib/jenkins

# Restart Jenkins to apply changes
sudo systemctl restart jenkins

# Wait for Jenkins to start
until curl -s "${JENKINS_URL}" >/dev/null; do
    sleep 5
done
