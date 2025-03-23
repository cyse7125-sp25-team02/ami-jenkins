import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig
import hudson.util.Secret
import java.util.Base64
import java.nio.charset.StandardCharsets
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import java.io.FileInputStream
import com.cloudbees.plugins.credentials.SecretBytes

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
String gcpServiceAccountKeyB64 = props.getProperty('GCP_SERVICE_ACCOUNT_KEY_B64')

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

def githubTokenCred = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github-token",
    "GitHub Token",
    Secret.fromString(githubToken)
)
store.addCredentials(domain, githubTokenCred)

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

String gcpServiceAccountKey = new String(Base64.decoder.decode(gcpServiceAccountKeyB64))
byte[] keyBytes = gcpServiceAccountKey.getBytes(StandardCharsets.UTF_8)
def secretBytes = SecretBytes.fromBytes(keyBytes)
def fileName = "gcp-service-account-key.json"

def gcpCred = new FileCredentialsImpl(
    CredentialsScope.GLOBAL,
    "gcp-service-account-key",
    "GCP Service Account Key",
    fileName,
    secretBytes
)
store.addCredentials(domain, gcpCred)

instance.save()
