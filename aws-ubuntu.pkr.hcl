packer {
  required_plugins {
    amazon = {
      version = ">= 1.3.4"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "JENKINS_ADMIN_USER" {
  type      = string
  sensitive = true
}

variable "JENKINS_ADMIN_PASSWORD" {
  type      = string
  sensitive = true
}

variable "JENKINS_URL" {
  type = string
}


source "amazon-ebs" "ubuntu" {
  ami_name              = "karan-jenkins-ami"
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
      "packer-scripts/install-certbot.sh"
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
    source      = "packer-scripts/plugins.yaml"
    destination = "/tmp/plugins.yaml"
  }

  provisioner "shell" {
    scripts = [
      "packer-scripts/file-permissions.sh",
      "packer-scripts/setup-jenkins.sh"
    ]
    environment_vars = [
      "JENKINS_ADMIN_USER=${var.JENKINS_ADMIN_USER}",
      "JENKINS_ADMIN_PASSWORD=${var.JENKINS_ADMIN_PASSWORD}",
      "JENKINS_URL=${var.JENKINS_URL}"
    ]
  }


}