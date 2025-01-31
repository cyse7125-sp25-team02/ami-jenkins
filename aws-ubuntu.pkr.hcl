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
    ]
  }
}