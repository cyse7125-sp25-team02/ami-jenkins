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

variable "GITHUB_CREDENTIALS_ID" {
  type      = string
  sensitive = true
}

variable "GITHUB_USERNAME" {
  type      = string
  sensitive = true
}

variable "GITHUB_TOKEN_ID" {
  type      = string
  sensitive = true
}

variable "GITHUB_TOKEN" {
  type      = string
  sensitive = true
}

variable "GITHUB_REPO_URL" {
  type = string
}

variable "GITHUB_ORG" {
  type = string
}

variable "GITHUB_REPO" {
  type = string
}
