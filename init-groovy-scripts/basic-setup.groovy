import jenkins.model.*
import hudson.security.*
import java.util.logging.Logger

def logger = Logger.getLogger("JenkinsSetupScript")

def props = new Properties()
def envFile = new File('/etc/jenkins.env')
if (envFile.exists()) {
    props.load(envFile.newDataInputStream())
} else {
    throw new RuntimeException("/etc/jenkins.env file not found")
}

def user = props.getProperty('JENKINS_ADMIN_USER')
def password = props.getProperty('JENKINS_ADMIN_PASSWORD')

if (!user || !password) {
    throw new RuntimeException("JENKINS_USER or JENKINS_PASSWORD is missing in /etc/jenkins.env")
}

def instance = Jenkins.instanceOrNull
if (instance == null) {
    throw new RuntimeException("Jenkins instance is null!")
}

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(user, password)
logger.info("Admin user '${user}' created '${password}' successfully.")
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
logger.info("Jenkins security configuration applied successfully.")
