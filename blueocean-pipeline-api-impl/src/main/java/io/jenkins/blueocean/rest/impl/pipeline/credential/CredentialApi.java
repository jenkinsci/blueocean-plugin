package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.google.common.collect.ImmutableList;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.CreateResponse;
import io.jenkins.blueocean.rest.model.Resource;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Credential API implementation.
 *
 * TODO: Remove it once proper REST API is implemented in Credentials plugin
 *
 * @author Vivek Pandey
 */
public class CredentialApi extends Resource {

    private  final CredentialsStoreAction credentialStoreAction;
    private  final Reachable parent;
    public static final String DOMAIN_NAME = "blueocean-domain";


    public CredentialApi(CredentialsStoreAction ca, Reachable parent) {
        this.credentialStoreAction = ca;
        this.parent = parent;

    }

    @Exported
    public String getStore(){
        return credentialStoreAction.getUrlName();
    }

    @POST
    @WebMethod(name = "")
    public CreateResponse create(@JsonBody JSONObject body, StaplerRequest request) throws IOException {

        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("No authenticated user found");
        }

        JSONObject jsonObject = body.getJSONObject("credentials");
        final IdCredentials credentials = request.bindJSON(IdCredentials.class, jsonObject);

        String domainName = DOMAIN_NAME;

        if(jsonObject.get("domain") != null && jsonObject.get("domain") instanceof String){
            domainName = (String) jsonObject.get("domain");
        }

        CredentialsUtils.createCredentialsInUserStore(credentials, authenticatedUser, domainName,
                ImmutableList.of(new BlueOceanDomainSpecification()));

        CredentialsStoreAction.DomainWrapper domainWrapper = credentialStoreAction.getDomain(domainName);


        if(domainWrapper != null) {
            CredentialsStoreAction.CredentialsWrapper credentialsWrapper = domainWrapper.getCredential(credentials.getId());
            if (credentialsWrapper != null){
                return new CreateResponse(
                        new CredentialApi.Credential(
                                credentialsWrapper,
                                getLink().rel("domains").rel(domainName).rel("credentials")));
            }
        }

        //this should never happen
        throw new ServiceException.UnexpectedErrorException("Unexpected error, failed to create credential");
    }


    @Navigable
    public Container<CredentialDomain> getDomains(){
        return new Container<CredentialDomain>() {
            private final Link self = CredentialApi.this.getLink().rel("domains");

            Map<String, CredentialsStoreAction.DomainWrapper> map = credentialStoreAction.getDomains();
            @Override
            public CredentialDomain get(String name) {
                return new CredentialDomain(map.get(name), getLink());
            }

            @Override
            public Link getLink() {
                return self;
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
                        return new CredentialDomain(i.next(), getLink());
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

        public CredentialDomain(CredentialsStoreAction.DomainWrapper domainWrapper, Link parent) {
            this.self = parent.rel(domainWrapper.getUrlName());
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
        public CredentialValueContainer getCredentials(){
            return new CredentialValueContainer(domainWrapper, this);
        }
    }

    public static class CredentialValueContainer extends Container<Credential>{
        private final CredentialsStoreAction.DomainWrapper domainWrapper;
        private final Link self;

        public CredentialValueContainer(CredentialsStoreAction.DomainWrapper domainWrapper, Reachable parent) {
            this.domainWrapper = domainWrapper;
            this.self = parent.getLink().rel("credentials");
        }

        @Override
        public Credential get(String name) {
            CredentialsStoreAction.CredentialsWrapper credentialsWrapper = domainWrapper.getCredential(name);
            if(credentialsWrapper != null) {
                return new Credential(credentialsWrapper, self);
            }
            throw new ServiceException.NotFoundException(String.format("Credential %s not found in domain %s", name,
                    domainWrapper.getFullName()));
        }

        @POST
        @WebMethod(name = "")
        @Deprecated
        public CreateResponse create(@JsonBody JSONObject body, StaplerRequest request) throws IOException {

            final IdCredentials credentials = request.bindJSON(IdCredentials.class, body.getJSONObject("credentials"));
            domainWrapper.getStore().addCredentials(domainWrapper.getDomain(), credentials);


            final Domain domain = domainWrapper.getDomain();
            domainWrapper.getStore().addCredentials(domain, credentials);

            return new CreateResponse(new Credential(domainWrapper.getCredentials().get(credentials.getId()), getLink()));
        }

        @Override
        public Link getLink() {
            return self;
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
                    return new Credential(i.next(),self);
                }

                @Override
                public void remove() {
                    throw new ServiceException.NotImplementedException("Not implemented yet");
                }
            };
        }
    }


    public static class Credential extends Resource{

        private final Link self;
        private final CredentialsStoreAction.CredentialsWrapper credentialsWrapper;

        public Credential(CredentialsStoreAction.CredentialsWrapper credentialsWrapper, Link parent) {
            this.self = parent.rel(credentialsWrapper.getUrlName());
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

        @Exported
        public String getDomain(){
            return credentialsWrapper.getDomain().getUrlName();
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
