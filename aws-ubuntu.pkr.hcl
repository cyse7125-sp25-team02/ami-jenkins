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
  instance_type         = "t2.micro"
  region                = "us-east-1"
  ssh_username          = "ubuntu"
  force_deregister      = true
  force_delete_snapshot = true
}

build {
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "shell" {
    scripts = [
      "packer-scripts/install-jenkins.sh",
      "packer-scripts/install-nginx.sh",
      "packer-scripts/create-config-file.sh",
      "packer-scripts/install-certbot.sh",
      "packer-scripts/install-terraform.sh"
    ]
    environment_vars = [
      "JENKINS_URL=${var.JENKINS_URL}",
      "JENKINS_ADMIN_USER=${var.JENKINS_ADMIN_USER}",
      "JENKINS_ADMIN_PASSWORD=${var.JENKINS_ADMIN_PASSWORD}"
    ]
  }

  provisioner "file" {
    source      = "packer-scripts/jenkins.yaml"
    destination = "/tmp/jenkins.yaml"
  }

  provisioner "file" {
    source      = "packer-scripts/z-create-pipeline-job.groovy"
    destination = "/tmp/z-create-pipeline-job.groovy"
  }

  provisioner "shell" {
    scripts = [
      "packer-scripts/file-permissions.sh",
      "packer-scripts/setup-jenkins.sh"
    ]
    environment_vars = [
      "JENKINS_ADMIN_USER=${var.JENKINS_ADMIN_USER}",
      "JENKINS_ADMIN_PASSWORD=${var.JENKINS_ADMIN_PASSWORD}",
      "JENKINS_URL=${var.JENKINS_URL}",
      "GITHUBB_CREDENTIALS_ID=${var.GITHUBB_CREDENTIALS_ID}",
      "GITHUBB_USERNAME=${var.GITHUBB_USERNAME}",
      "GITHUBB_TOKEN_ID=${var.GITHUBB_TOKEN_ID}",
      "GITHUBB_TOKEN=${var.GITHUBB_TOKEN}",
      "GITHUBB_REPO_URL=${var.GITHUBB_REPO_URL}",
      "GITHUBB_ORG=${var.GITHUBB_ORG}",
      "GITHUBB_REPO=${var.GITHUBB_REPO}"
    ]
  }
}
