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
      "sudo chown -R ubuntu:ubuntu /opt/jenkins-files"
    ]
  }

  provisioner "file" {
    source      = "jenkins-config.zip"
    destination = "/opt/jenkins-files/jenkins-config.zip"
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
      "sudo echo 'GITHUBB_WEBHOOK_SECRET=${var.GITHUBB_WEBHOOK_SECRET}' | sudo tee -a /etc/jenkins.env > /dev/null",
      "sudo echo 'GCP_SERVICE_ACCOUNT_KEY_B64=${base64encode(var.GCP_SERVICE_ACCOUNT_KEY)}' | sudo tee -a /etc/jenkins.env > /dev/null",
    ]
  }

  provisioner "shell" {
    scripts = [
      "shell-scripts/install-jenkins.sh",
      "shell-scripts/install-nginx.sh",
      "shell-scripts/create-config-file.sh",
      "shell-scripts/install-unzip.sh",
      "shell-scripts/setup-jenkins.sh",
      "shell-scripts/install-certbot.sh",
      "shell-scripts/install-terraform.sh",
      "shell-scripts/install-docker.sh",
      "shell-scripts/install-gcloud.sh",
      "shell-scripts/install-node.sh",
    ]
  }
}
