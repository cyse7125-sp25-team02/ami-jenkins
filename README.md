# ami-jenkins

This is the repository to create Jenkins AMI using Hashicorp Packer

### Application Dependencies
1. Hashicorp Packer installed in your computer
2. AWS account with AWS configured in your local

### Steps to run the application

1. Fork the repository in your GitHub account and clone the forked repository in your local
2. Run following commands one by one:
   ```
   packer init .
   packer validate .
   packer build .
   ```
3. After packer build succeeds, you will see the AMI named `jenkins-ami` on your AWS console
