package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.ViewCredentialsAction;
import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.OrganizationRoute;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Credential API container
 *
 * TODO: can very well be moved in it's own plugin along with {@link CredentialApi}
 *
 * @author Vivek Pandey
 */
@Extension
@ExportedBean
public class CredentialContainer extends Container<CredentialApi> implements OrganizationRoute {
    private final Link self;

    public CredentialContainer() {
        BlueOrganization organization=null;
        for(BlueOrganization action: ExtensionList.lookup(BlueOrganization.class)){
            organization = action;
        };
        this.self = (organization != null) ? organization.getLink().rel("credentials")
                : new Link("/organizations/jenkins/credentials/");
    }

    @Override
    public String getUrlName() {
        return "credentials";
    }


    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public CredentialApi get(String name) {
        for(CredentialApi api: this){
            if(api.getStore().equals(name)){
                return api;
            }
        }
        return null;
    }

    @Override
    public Iterator<CredentialApi> iterator() {
        List<CredentialApi> apis = new ArrayList<>();
        for(ViewCredentialsAction action: ExtensionList.lookup(ViewCredentialsAction.class)){
            for(CredentialsStoreAction c:action.getStoreActions()){
                apis.add(new CredentialApi(c, this));
            }
        };
        return apis.iterator();
    }

}