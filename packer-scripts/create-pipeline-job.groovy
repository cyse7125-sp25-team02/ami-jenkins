import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.*
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.github.pullrequest.GitHubPRTrigger

def jobName = 'Terraform PR Validation'

// Fetch environment variables explicitly before using them
def GITHUB_ORG = System.getenv('GITHUB_ORG') ?: ''
def GITHUB_REPO = System.getenv('GITHUB_REPO') ?: ''
def GITHUB_CREDENTIALS_ID = System.getenv('GITHUB_CREDENTIALS_ID') ?: ''
def GITHUB_REPO_URL = System.getenv('GITHUB_REPO_URL') ?: ''
def GITHUB_TOKEN_ID = System.getenv('GITHUB_TOKEN_ID') ?: ''

// Construct pipeline script as a string with interpolated values
def pipelineScript = """
node('any') {
    properties([
        pipelineTriggers([
            githubPullRequests(
                spec: '',
                triggerMode: 'HEAVY_HOOKS',
                repoProviders: [githubPlugin(
                    serverUrl: 'https://api.github.com',
                    repoOwner: '${GITHUB_ORG}',
                    repository: '${GITHUB_REPO}',
                    credentialsId: '${GITHUB_CREDENTIALS_ID}'
                )]
            )
        ])
    ])
    
    timeout(time: 15, unit: 'MINUTES') {
        try {
            stage('Checkout') {
                checkout([
                    \$class: 'GitSCM',
                    branches: [[name: env.CHANGE_BRANCH]],
                    extensions: [],
                    userRemoteConfigs: [
                        [
                            url: '${GITHUB_REPO_URL}',
                            credentialsId: '${GITHUB_CREDENTIALS_ID}'
                        ]
                    ]
                ])
            }
            
            stage('Terraform Init') {
                sh 'terraform init'
            }
            
            stage('Terraform Format Check') {
                sh 'terraform fmt -check'
            }
            
            stage('Terraform Validate') {
                sh 'terraform validate'
            }
        } finally {
            deleteDir()
            
            if (env.CHANGE_ID) {
                def statusMessage = """
                **Terraform Validation Results**
                - Status: \${currentBuild.result == 'SUCCESS' ? '✅ Success' : '❌ Failure'}
                - Build Number: #\${env.BUILD_NUMBER}
                - Console Log: \${env.RUN_DISPLAY_URL}
                """
                
                withCredentials([string(credentialsId: '${GITHUB_TOKEN_ID}', variable: 'GITHUB_TOKEN')]) {
                    sh """
                    curl -sS -X POST \\
                        -H "Authorization: token \$GITHUB_TOKEN" \\
                        -H "Accept: application/vnd.github.v3+json" \\
                        "https://api.github.com/repos/${GITHUB_ORG}/${GITHUB_REPO}/issues/\${env.CHANGE_ID}/comments" \\
                        -d "{\\"body\\": \\"${statusMessage.replace('"', '\\\\"')}\\"}"
                    """
                }
            }
        }
    }
}
"""

// Job creation with proper initialization
def jenkins = Jenkins.getInstanceOrNull()
if (jenkins == null) {
    throw new IllegalStateException("Jenkins instance is null. Ensure Jenkins is running.")
}

def job = jenkins.getItem(jobName) ?: new WorkflowJob(jenkins, jobName)
job.definition = new CpsFlowDefinition(pipelineScript, true)

if (jenkins.getItem(jobName) == null) {
    jenkins.add(job, jobName)
}

jenkins.save()
jenkins.reload()
