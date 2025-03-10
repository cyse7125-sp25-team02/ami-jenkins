import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*

Properties props = new Properties()
File envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

String githubUsername = props.getProperty('GITHUBB_USERNAME')
String githubToken = props.getProperty('GITHUBB_TOKEN')
String dockerUsername = props.getProperty('DOCKER_USERNAME')
String dockerToken = props.getProperty('DOCKER_TOKEN')

def instance = Jenkins.instance
def domain = Domain.global()
def store = instance.getExtensionList("com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()

def githubCred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github-credentials",
    "GitHub Credentials",
    githubUsername,
    githubToken
)
store.addCredentials(domain, githubCred)

def dockerCred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "docker-hub-credentials",
    "Docker Hub Credentials",
    dockerUsername,
    dockerToken
)
store.addCredentials(domain, dockerCred)

instance.save()
