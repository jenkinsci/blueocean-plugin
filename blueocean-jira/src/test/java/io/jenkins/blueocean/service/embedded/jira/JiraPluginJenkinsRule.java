package io.jenkins.blueocean.service.embedded.jira;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.util.security.Password;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author Vivek Pandey
 */
public class JiraPluginJenkinsRule extends JenkinsRule {
    @Override
    protected LoginService configureUserRealm() {
        HashLoginService realm = new HashLoginService();
        realm.setName("default");   // this is the magic realm name to make it effective on everywhere
        realm.update("alice", new Password("alice"), new String[]{"user","female"});
        realm.update("bob", new Password("bob"), new String[]{"user","male"});
        realm.update("charlie", new Password("charlie"), new String[]{"user","male"});
        return realm;
    }
}
