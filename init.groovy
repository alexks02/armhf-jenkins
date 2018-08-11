import hudson.model.*;
import hudson.security.*
import jenkins.model.*;


Thread.start {
      sleep 10000
      println "--> setting agent port for jnlp"
      def env = System.getenv()
      int port = env['JENKINS_SLAVE_AGENT_PORT'].toInteger()
      Jenkins.instance.setSlaveAgentPort(port)
      println "--> setting agent port for jnlp... done"
}

Thread.start {
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
      println "--> creating admin user"
}
