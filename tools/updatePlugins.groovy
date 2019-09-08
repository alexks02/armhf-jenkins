import jenkins.model.*
import hudson.model.UpdateCenter

//Check for the latest update center updates from jenkins.io
Jenkins.instance.pluginManager.doCheckUpdatesServer()

//get the current update center
UpdateCenter center = Jenkins.instance.updateCenter

def installed = false

//schedule an upgrade of all plugins
print "Upgrading Plugins: "
println center.updates.findAll { center.getJob(it) == null }.each {
  it.deploy(false)
  installed = true
}*.name

if (installed) {
    println "Plugins installed, initializing a restart!"
    instance.save()
    instance.doSafeRestart()
}