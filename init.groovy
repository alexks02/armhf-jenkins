import jenkins.*
import hudson.model.*;
import hudson.security.*
import jenkins.model.*;
import jenkins.security.s2m.*

def j = Jenkins.instance
def env = System.getenv()

// Agent port for JNLP
sleep 10000
int port = env['JENKINS_SLAVE_AGENT_PORT'].toInteger()
j.setSlaveAgentPort(port)
println "--> Setting agent port for jnlp"

// Creating admin user
def adminUsername = env['ADMIN_USERNAME']
def adminPassword = env['ADMIN_PASSWORD']
assert adminPassword != null : "No ADMIN_USERNAME env var provided, but required"
assert adminPassword != null : "No ADMIN_PASSWORD env var provided, but required"
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUsername, adminPassword)
j.setSecurityRealm(hudsonRealm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
j.setAuthorizationStrategy(strategy)
println "--> Creating admin user"

// Set Jenkins URL
def location = j.getExtensionList('jenkins.model.JenkinsLocationConfiguration')[0]
def jenkinsUrl = env['JENKINS_URL']
assert jenkinsUrl != null : "No JENKINS_URL env var provided, but required"
location.url = jenkinsUrl
location.save()
println "--> Set Jenkins URL"

// Disable agent -> master security
def rule = j.getExtensionList('jenkins.security.s2m.MasterKillSwitchConfiguration')[0].rule
rule.setMasterKillSwitch(true)
j.getExtensionList('jenkins.security.s2m.MasterKillSwitchWarning')[0].disable(false)
j.getDescriptor("jenkins.CLI").get().setEnabled(false)
println "--> Disable agent -> master security for cobertura"

// Set 'Enable Slave -> Master Access Control'
j.injector.getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
println "--> Set 'Enable Slave -> Master Access Control' option"

// Set English locale for all users
def pluginWrapper = j.getPluginManager().getPlugin('locale')
def plugin = pluginWrapper.getPlugin()
plugin.setSystemLocale('en')
plugin.ignoreAcceptLanguage = true
println "--> Set English locale for all users"

// Save all this shit
j.save()
