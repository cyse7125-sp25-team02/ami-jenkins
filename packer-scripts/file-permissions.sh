#!/bin/bash
sudo mkdir -p /var/lib/jenkins/init.groovy.d
sudo mv /tmp/*.yaml /var/lib/jenkins/
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo chmod -R 755 /var/lib/jenkins
sudo chmod 644 /var/lib/jenkins/*.yaml
