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

variable "GITHUBB_CREDENTIALS_ID" {
  type      = string
  sensitive = true
}

variable "GITHUBB_USERNAME" {
  type      = string
  sensitive = true
}

variable "GITHUBB_TOKEN_ID" {
  type      = string
  sensitive = true
}

variable "GITHUBB_TOKEN" {
  type      = string
  sensitive = true
}

variable "GITHUBB_REPO_URL" {
  type = string
}

variable "GITHUBB_ORG" {
  type = string
}

variable "GITHUBB_REPO" {
  type = string
}

variable "STATIC_SITE_REPO" {
  type = string
}

variable "DOCKER_USERNAME" {
  type = string
}

variable "DOCKER_TOKEN" {
  type      = string
  sensitive = true
}

variable "DOCKER_IMAGE" {
  type = string
}
