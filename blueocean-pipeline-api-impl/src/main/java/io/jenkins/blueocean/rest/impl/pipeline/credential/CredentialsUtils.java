package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.domains.HostnamePortSpecification;
import com.cloudbees.plugins.credentials.domains.HostnameSpecification;
import com.cloudbees.plugins.credentials.domains.PathSpecification;
import com.cloudbees.plugins.credentials.domains.SchemeSpecification;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class CredentialsUtils {

    public static void createCredentialsInUserStore(@Nonnull Credentials credential, @Nonnull User user,
                                                    @Nonnull String domainName, @Nullable URI uri)
            throws IOException {
        CredentialsStore store= findUserStore(user);

        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }

        Domain domain = findOrCreateDomain(store, domainName, uri);

        if(!store.addCredentials(domain, credential)){
            throw new ServiceException.UnexpectedErrorException("Failed to add credential to domain");
        }

    }

    public static void updateCredentialsInUserStore(@Nonnull Credentials current, @Nonnull Credentials replacement,
                                                    @Nonnull User user,
                                                    @Nonnull String domainName, @Nullable URI uri)
            throws IOException {
        CredentialsStore store= findUserStore(user);

        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }

        Domain domain = findOrCreateDomain(store, domainName, uri);

        if(!store.updateCredentials(domain, current, replacement)){
            throw new ServiceException.UnexpectedErrorException("Failed to update credential to domain");
        }
    }

    public static @CheckForNull CredentialsStore findUserStore(User user){
        for(CredentialsStore s: CredentialsProvider.lookupStores(user)){
            if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                return s;
            }
        }
        return null;
    }

    private static @Nonnull Domain findOrCreateDomain(@Nonnull CredentialsStore store, @Nonnull String domainName, @Nullable URI uri) throws IOException {

        Domain domain = store.getDomainByName(domainName);
        if (domain == null) { //create new one
            List<DomainSpecification> domainSpecifications = new ArrayList<>();

            if (uri != null) {
                // XXX: UriRequirementBuilder.fromUri() maps "" path to "/", so need to take care of it here
                String path = uri.getRawPath() == null ? null : (uri.getRawPath().trim().isEmpty() ? "/" : uri.getRawPath());
                domainSpecifications.add(new PathSpecification(path, "", false));
                if (uri.getPort() != -1) {
                    domainSpecifications.add(new HostnamePortSpecification(uri.getHost() + ":" + uri.getPort(), null));
                } else {
                    domainSpecifications.add(new HostnameSpecification(uri.getHost(), null));
                }
                domainSpecifications.add(new SchemeSpecification(uri.getScheme()));
            }
            boolean result = store.addDomain(new Domain(domainName,
                    "Github Domain to store personal access token",
                    domainSpecifications
            ));
            if (!result) {
                throw new ServiceException.BadRequestExpception("Failed to create credential domain: " + domainName);
            }
            domain = store.getDomainByName(domainName);
            if (domain == null) {
                throw new ServiceException.UnexpectedErrorException(String.format("Domain %s created but not found"));
            }
        }
        return domain;
    }
}
