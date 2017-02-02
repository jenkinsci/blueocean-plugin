package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.domains.HostnamePortSpecification;
import com.cloudbees.plugins.credentials.domains.HostnameSpecification;
import com.cloudbees.plugins.credentials.domains.PathSpecification;
import com.cloudbees.plugins.credentials.domains.SchemeSpecification;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class CredentialsUtils {

    public static void createCredentialsInUserStore(@Nonnull Credentials credential, @Nonnull User user,
                                                    @Nonnull String domainName, @Nullable URI uri)
            throws IOException {
        CredentialsStore store= findUserStoreFirstOrNull(user);

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
        CredentialsStore store= findUserStoreFirstOrNull(user);

        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }

        Domain domain = findOrCreateDomain(store, domainName, uri);

        if(!store.updateCredentials(domain, current, replacement)){
            throw new ServiceException.UnexpectedErrorException("Failed to update credential to domain");
        }
    }

    public static @CheckForNull Domain findDomain(@Nonnull final String credentialId, @Nonnull User user){
        for(final CredentialsStore store: findUserStores(user)) {
            Optional<Domain> d = Iterables.tryFind(store.getDomains(), new Predicate<Domain>() {
                @Override
                public boolean apply(@Nullable Domain input) {
                    return input != null && Iterables.tryFind(store.getCredentials(input), new Predicate<Credentials>() {
                        @Override
                        public boolean apply(@Nullable Credentials input) {
                            return (input != null && input instanceof IdCredentials) && ((IdCredentials) input).getId().equals(credentialId);
                        }
                    }).isPresent();

                }
            });
            if (d.isPresent()) {
                return d.get();
            }
        }
        return null;
    }

    public static @CheckForNull <C extends Credentials> C  findCredential(@Nonnull String id, @Nonnull Class<C> type){
        if(User.current() == null){
            throw new ServiceException.UnauthorizedException("No authenticated user found. Please login");
        }

        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        type,
                        Jenkins.getInstance(),
                        Jenkins.getAuthentication(),
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(id))
        );
    }

    public static @CheckForNull <C extends Credentials> C findCredential(@Nonnull String credentialId, @Nullable String uri, @Nonnull Class<C> type){
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        type,
                        Jenkins.getInstance(),
                        Jenkins.getAuthentication(),
                        URIRequirementBuilder.fromUri(uri).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId))
        );
    }

    private static @CheckForNull CredentialsStore findUserStoreFirstOrNull(User user){
        for(CredentialsStore s: CredentialsProvider.lookupStores(user)){
            if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                return s;
            }
        }
        return null;
    }

    private static @CheckForNull Iterable<CredentialsStore> findUserStores(User user){
        return Iterables.filter(CredentialsProvider.lookupStores(user), new Predicate<CredentialsStore>() {
            @Override
            public boolean apply(@Nullable CredentialsStore s) {
                return s!= null && s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE);
            }
        });

    }

    private static List<DomainSpecification> generateDomainSpecifications(@Nullable URI uri){
        if (uri == null) {
            return Collections.emptyList();
        }

        List<DomainSpecification> domainSpecifications = new ArrayList<>();

        // XXX: UriRequirementBuilder.fromUri() maps "" path to "/", so need to take care of it here
        String path = uri.getRawPath() == null ? null : (uri.getRawPath().trim().isEmpty() ? "/" : uri.getRawPath());

        domainSpecifications.add(new PathSpecification(path, "", false));
        if (uri.getPort() != -1) {
            domainSpecifications.add(new HostnamePortSpecification(uri.getHost() + ":" + uri.getPort(), null));
        } else {
            domainSpecifications.add(new HostnameSpecification(uri.getHost(), null));
        }
        domainSpecifications.add(new SchemeSpecification(uri.getScheme()));
        return domainSpecifications;
    }

    private static @Nonnull Domain findOrCreateDomain(@Nonnull CredentialsStore store, @Nonnull String domainName, @Nullable URI uri) throws IOException {

        Domain domain = store.getDomainByName(domainName);
        if (domain == null) { //create new one
            List<DomainSpecification> domainSpecifications = generateDomainSpecifications(uri);
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
