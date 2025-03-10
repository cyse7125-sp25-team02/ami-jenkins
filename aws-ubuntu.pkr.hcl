packer {
  required_plugins {
    amazon = {
      version = ">= 1.3.4"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  ami_name              = "jenkins-ami"
  source_ami            = "ami-04b4f1a9cf54c11d0"
  instance_type         = "t2.small"
  region                = "us-east-1"
  ssh_username          = "ubuntu"
  force_deregister      = true
  force_delete_snapshot = true
}

build {
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "shell" {
    inline = [
      "sudo mkdir -p /opt/jenkins-files",
      "sudo chown ubuntu:ubuntu /opt/jenkins-files"
    ]
  }

  provisioner "file" {
    source      = "init-groovy-scripts/basic-setup.groovy"
    destination = "/opt/jenkins-files/basic-setup.groovy"
  }

  provisioner "file" {
    source      = "init-groovy-scripts/install-plugins.groovy"
    destination = "/opt/jenkins-files/install-plugins.groovy"
  }

  provisioner "file" {
    source      = "init-groovy-scripts/credentials.groovy"
    destination = "/opt/jenkins-files/credentials.groovy"
  }

  provisioner "file" {
    source      = "multibranch-pipeline-jobs/infra-jenkins/terraform-validation.groovy"
    destination = "/opt/jenkins-files/terraform-validation.groovy"
  }

  provisioner "file" {
    source      = "jcasc.yaml"
    destination = "/opt/jenkins-files/jcasc.yaml"
  }

  provisioner "shell" {
    inline = [
      "sudo echo 'JENKINS_ADMIN_USER=${var.JENKINS_ADMIN_USER}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'JENKINS_ADMIN_PASSWORD=${var.JENKINS_ADMIN_PASSWORD}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'GITHUBB_CREDENTIALS_ID=${var.GITHUBB_CREDENTIALS_ID}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'GITHUBB_USERNAME=${var.GITHUBB_USERNAME}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'GITHUBB_TOKEN=${var.GITHUBB_TOKEN}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'GITHUBB_ORG=${var.GITHUBB_ORG}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'DOCKER_USERNAME=${var.DOCKER_USERNAME}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'DOCKER_TOKEN=${var.DOCKER_TOKEN}' | sudo tee -a /etc/jenkins.env > /dev/null",
    ]
  }

  provisioner "shell" {
    scripts = [
      "packer-scripts/install-jenkins.sh",
      "packer-scripts/install-nginx.sh",
      "packer-scripts/create-config-file.sh",
      "packer-scripts/setup-jenkins.sh",
      "packer-scripts/install-certbot.sh",
      "packer-scripts/install-terraform.sh",
      "packer-scripts/install-docker.sh",
    ]
  }

  provisioner "shell" {
    inline = [
      "sudo systemctl daemon-reload",
      "sudo systemctl restart jenkins"
    ]
  }
}
