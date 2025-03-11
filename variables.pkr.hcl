variable "JENKINS_ADMIN_USER" {
  type      = string
  sensitive = true
}

variable "JENKINS_ADMIN_PASSWORD" {
  type      = string
  sensitive = true
}

variable "GITHUBB_CREDENTIALS_ID" {
  type      = string
  sensitive = true
}

variable "GITHUBB_USERNAME" {
  type      = string
  sensitive = true
}

variable "GITHUBB_TOKEN" {
  type      = string
  sensitive = true
}

variable "GITHUBB_ORG" {
  type = string
}

variable "DOCKER_USERNAME" {
  type = string
}

variable "DOCKER_TOKEN" {
  type      = string
  sensitive = true
}

variable "GITHUBB_WEBHOOK_SECRET" {
  type      = string
  sensitive = true
}
