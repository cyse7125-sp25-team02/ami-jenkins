import jenkins.model.*
import hudson.security.*
import java.util.logging.Logger

// Logger for debugging
def logger = Logger.getLogger("JenkinsSetupScript")

// Load environment variables from /etc/jenkins.env
def props = new Properties()
def envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

def user = props.getProperty('JENKINS_ADMIN_USER')
def password = props.getProperty('JENKINS_ADMIN_PASSWORD')

// Validate username and password
if (!user || !password) {
    throw new RuntimeException("JENKINS_USER or JENKINS_PASSWORD is missing in /etc/jenkins.env")
}

def instance = Jenkins.instanceOrNull
if (instance == null) {
    throw new RuntimeException("Jenkins instance is null!")
}

// Configure security realm
def hudsonRealm = new HudsonPrivateSecurityRealm(false)

// Check if user already exists to prevent recreation

hudsonRealm.createAccount(user, password)
logger.info("Admin user '${user}' created '${password}' successfully.")

    


instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
logger.info("Jenkins security configuration applied successfully.")
