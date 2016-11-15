package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.Api;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Credential API implementation.
 *
 * TODO: it can very well be moved out in to it's own plugin or pulled inside Credentials plugin as well.
 *
 * @author Vivek Pandey
 */
public class CredentialApi extends Resource {

    private  final CredentialsStoreAction credentialStoreAction;
    private  final Reachable parent;

    public CredentialApi(CredentialsStoreAction ca, Reachable parent) {
        this.credentialStoreAction = ca;
        this.parent = parent;

    }

    @Exported
    public String getStore(){
        return credentialStoreAction.getUrlName();
    }

    @Navigable
    public Container<CredentialDomain> getDomains(){
        return new Container<CredentialDomain>() {
            Map<String, CredentialsStoreAction.DomainWrapper> map = credentialStoreAction.getDomains();
            @Override
            public CredentialDomain get(String name) {
                return new CredentialDomain(map.get(name), this);
            }

            @Override
            public Link getLink() {
                return CredentialApi.this.getLink();
            }

            @Override
            public Iterator<CredentialDomain> iterator() {
                final Iterator<CredentialsStoreAction.DomainWrapper> i = map.values().iterator();
                return new Iterator<CredentialDomain>(){

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public CredentialDomain next() {
                        return new CredentialDomain(i.next(), CredentialApi.this);
                    }

                    @Override
                    public void remove() {
                        throw new ServiceException.NotImplementedException("Not implemented yet");
                    }
                };
            }
        };
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel(getStore());
    }

    public static class CredentialDomain extends Resource{

        private final Link self;
        private final CredentialsStoreAction.DomainWrapper domainWrapper;

        public CredentialDomain(CredentialsStoreAction.DomainWrapper domainWrapper, Reachable parent) {
            this.self = parent.getLink().rel(domainWrapper.getUrlName());
            this.domainWrapper = domainWrapper;
        }

        @Exported(inline = true, merge = true)
        public CredentialsStoreAction.DomainWrapper getDomain(){
            return domainWrapper;
        }

        @Override
        public Link getLink() {
            return self;
        }

        @Navigable
        public Container<Credential> getCredentials(){
            return new Container<Credential>() {
                @Override
                public Credential get(String name) {
                    return new Credential(domainWrapper.getCredential(name), this);
                }

                @POST
                @WebMethod(name = "")
                public HttpResponse create(@JsonBody final IdCredentials credentials) throws IOException {
                    final Domain domain = domainWrapper.getDomain();
                    domainWrapper.getStore().addCredentials(domain, credentials);
                    return new HttpResponse() {
                        @Override
                        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                            new Api(domainWrapper.getCredentials().get(credentials.getId())).doJson(req,rsp);
                        }
                    };

                }

                @Override
                public Link getLink() {
                    return CredentialDomain.this.getLink().rel("credentials");
                }

                @Override
                public Iterator<Credential> iterator() {
                    final Iterator<CredentialsStoreAction.CredentialsWrapper> i = domainWrapper.getCredentialsList().iterator();
                    return new Iterator<Credential>(){

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public Credential next() {
                            return new Credential(i.next(),CredentialDomain.this);
                        }

                        @Override
                        public void remove() {
                            throw new ServiceException.NotImplementedException("Not implemented yet");
                        }
                    };
                }
            };
        }
    }

    public static class Credential extends Resource{

        private final Link self;
        private final CredentialsStoreAction.CredentialsWrapper credentialsWrapper;

        public Credential(CredentialsStoreAction.CredentialsWrapper credentialsWrapper, Reachable parent) {
            this.self = parent.getLink().rel(credentialsWrapper.getUrlName());
            this.credentialsWrapper = credentialsWrapper;
        }

        @Exported(merge = true, inline = true)
        public CredentialsStoreAction.CredentialsWrapper getCredential(){
            return credentialsWrapper;
        }


        @Override
        public Link getLink() {
            return self;
        }

        /**
         * If description is empty its displayName:domain:type, otherwise just the given description name
         */
        @Exported
        public String getDescription(){
            if(credentialsWrapper.getDescription() == null || credentialsWrapper.getDescription().trim().isEmpty()){
                return String.format("%s:%s:%s",credentialsWrapper.getDisplayName(),credentialsWrapper.getDomain().getUrlName(), credentialsWrapper.getTypeName());
            }
            return credentialsWrapper.getDescription();
        }
    }

}
