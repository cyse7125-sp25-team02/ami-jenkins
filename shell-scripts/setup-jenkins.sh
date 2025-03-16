#!/bin/bash
set -x

sudo mkdir -p /var/lib/jenkins/init.groovy.d
sudo chown -R jenkins:jenkins /opt/jenkins-files

sudo mv /opt/jenkins-files/init-groovy-scripts/basic-setup.groovy /var/lib/jenkins/init.groovy.d/basic-setup.groovy
sudo chown -R jenkins:jenkins /var/lib/jenkins/init.groovy.d/
sudo chmod 755 /var/lib/jenkins/init.groovy.d/basic-setup.groovy

sudo mv /opt/jenkins-files/init-groovy-scripts/install-plugins.groovy /var/lib/jenkins/init.groovy.d/install-plugins.groovy
sudo chown -R jenkins:jenkins /var/lib/jenkins/init.groovy.d/
sudo chmod 755 /var/lib/jenkins/init.groovy.d/install-plugins.groovy

sudo systemctl restart jenkins

sudo chmod 644 /etc/jenkins.env
sudo chown jenkins:jenkins /etc/jenkins.env

sudo mkdir -p /var/lib/jenkins/casc_configs
sudo mv /opt/jenkins-files/jcasc.yaml /var/lib/jenkins/casc_configs/jcasc.yaml
sudo chown -R jenkins:jenkins /var/lib/jenkins/casc_configs/
sudo chmod 755 /var/lib/jenkins/casc_configs/jcasc.yaml

sudo mv /opt/jenkins-files/init-groovy-scripts/credentials.groovy /var/lib/jenkins/init.groovy.d/credentials.groovy
sudo chown -R jenkins:jenkins /var/lib/jenkins/init.groovy.d/
sudo chmod 755 /var/lib/jenkins/init.groovy.d/credentials.groovy

echo 'CASC_JENKINS_CONFIG="/var/lib/jenkins/casc_configs/jcasc.yaml"' | sudo tee -a /etc/environment
sudo sed -i 's/\(JAVA_OPTS=-Djava\.awt\.headless=true\)/\1 -Djenkins.install.runSetupWizard=false/' /lib/systemd/system/jenkins.service
sudo sed -i '/Environment="JAVA_OPTS=-Djava.awt.headless=true -Djenkins.install.runSetupWizard=false"/a Environment="CASC_JENKINS_CONFIG=/var/lib/jenkins/casc_configs/jcasc.yaml"' /lib/systemd/system/jenkins.service

sudo systemctl daemon-reload
sudo systemctl restart jenkins
