package io.jenkins.blueocean;

import hudson.Extension;
import io.jenkins.blueocean.security.LoginAction;
import io.jenkins.embryo.App;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanUI extends App {
    public HttpResponse doHello() {
        return HttpResponses.plainText("Hello wo1rlsd!");
    }

    public LoginAction getLoginAction() {
        return new LoginAction();
    }
}
