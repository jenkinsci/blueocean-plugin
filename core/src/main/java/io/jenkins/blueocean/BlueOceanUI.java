package io.jenkins.blueocean;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanUI {
    public HttpResponse doHello() {
        return HttpResponses.plainText("Hello wolrd!");
    }

    public Object getRest(){
        try {
            ExtensionList extensions = Jenkins.getActiveInstance().getExtensionList("io.jenkins.blueocean.rest.ApiHead");
            if(extensions.size() > 0){
                return extensions.get(0);
            }else{
                return HttpResponses.notFound();
            }
        } catch (ClassNotFoundException e) {
            return HttpResponses.notFound();
        }

    }
}
