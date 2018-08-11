import jenkins.*
import hudson.model.*;
import hudson.security.*
import jenkins.model.*;
import jenkins.security.s2m.*


sleep 10000
println "--> setting agent port for jnlp"
def env = System.getenv()
int port = env['JENKINS_SLAVE_AGENT_PORT'].toInteger()
Jenkins.instance.setSlaveAgentPort(port)
println "--> setting agent port for jnlp... done"


println "--> creating admin user"
def adminUsername = System.getenv("ADMIN_USERNAME")
def adminPassword = System.getenv("ADMIN_PASSWORD")
assert adminPassword != null : "No ADMIN_USERNAME env var provided, but required"
assert adminPassword != null : "No ADMIN_PASSWORD env var provided, but required"

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUsername, adminPassword)
Jenkins.instance.setSecurityRealm(hudsonRealm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
Jenkins.instance.setAuthorizationStrategy(strategy)

Jenkins.instance.save()
println "--> creating admin user... done"


println "--> disable Jenkins CLI"
      
// disabled CLI access over TCP listener (separate port)
def p = AgentProtocol.all()
p.each { x ->
    if (x.name?.contains("CLI")) {
        println "Removing protocol ${x.name}"
        p.remove(x)
    }
}

// disable CLI access over /cli URL
def removal = { lst ->
    lst.each { x ->
        if (x.getClass().name.contains("CLIAction")) {
            println "Removing extension ${x.getClass().name}"
            lst.remove(x)
        }
    }
}
def j = Jenkins.instance
removal(j.getExtensionList(RootAction.class))
removal(j.actions)
println "--> disable Jenkins CLI... done"


println "--> set Jenkins URL"
def location = j.getExtensionList('jenkins.model.JenkinsLocationConfiguration')[0]
def jenkinsUrl = System.getenv("JENKINS_URL")
assert jenkinsUrl != null : "No JENKINS_URL env var provided, but required"
location.url = jenkinsUrl
j.save()
location.save()
println "--> set Jenkins URL... done"


def rule = j.getExtensionList(jenkins.security.s2m.MasterKillSwitchConfiguration.class)[0].rule
if (!rule.getMasterKillSwitch()) {
    rule.setMasterKillSwitch(true)
    //dismiss the warning because we don't care (cobertura reporting is broken otherwise)
    j.getExtensionList(jenkins.security.s2m.MasterKillSwitchWarning.class)[0].disable(false)
//////!!!
    j.getDescriptor("jenkins.CLI").get().setEnabled(false)
    j.save()
    println 'Disabled agent -> master security for cobertura.'
} else {
    println 'Nothing changed.  Agent -> master security already disabled.'
}


// define protocols
HashSet<String> oldProtocols = new HashSet<>(j.getAgentProtocols())
oldProtocols.removeAll(Arrays.asList("JNLP3-connect", "JNLP2-connect", "JNLP-connect", "CLI-connect"))

// set protocols
j.setAgentProtocols(oldProtocols)

// save to disk
j.save()

