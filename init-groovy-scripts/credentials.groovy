import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig
import hudson.util.Secret

Properties props = new Properties()
File envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

String githubUsername = props.getProperty('GITHUBB_USERNAME')
String githubToken = props.getProperty('GITHUBB_TOKEN')
String githubWebhookSecret = props.getProperty('GITHUBB_WEBHOOK_SECRET')
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


def githubWebhookCred = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github-webhook-secret",
    "GitHub Webhook Secret",
    Secret.fromString(githubWebhookSecret)
)
store.addCredentials(domain, githubWebhookCred)

def githubWebhookCredId = "github-webhook-secret"
def githubPluginConfig = Jenkins.instance.getExtensionList(GitHubPluginConfig.class)[0]
def hookSecretConfigs = new ArrayList<>(githubPluginConfig.getHookSecretConfigs() ?: [])

if (!hookSecretConfigs.any { it.credentialsId == githubWebhookCredId }) {
    hookSecretConfigs.add(new HookSecretConfig(githubWebhookCredId))
    githubPluginConfig.setHookSecretConfigs(hookSecretConfigs)
    githubPluginConfig.save()

    println "Shared secret '${githubWebhookCredId}' added successfully."
} else {
    println "Shared secret '${githubWebhookCredId}' already exists."
}

instance.save()
