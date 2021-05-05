package io.jenkins.blueocean.credential;

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
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Credentials utility
 *
 * @author Vivek Pandey
 */
public class CredentialsUtils {

    public static void createCredentialsInUserStore(@Nonnull Credentials credential, @Nonnull User user,
                                                    @Nonnull String domainName, @Nonnull List<DomainSpecification> domainSpecifications)
            throws IOException {
        CredentialsStore store= findUserStoreFirstOrNull(user);

        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }

        Domain domain = findOrCreateDomain(store, domainName, domainSpecifications);

        if(!store.addCredentials(domain, credential)){
            throw new ServiceException.UnexpectedErrorException("Failed to add credential to domain");
        }

    }

    public static void updateCredentialsInUserStore(@Nonnull Credentials current, @Nonnull Credentials replacement,
                                                    @Nonnull User user,
                                                    @Nonnull String domainName, @Nonnull List<DomainSpecification> domainSpecifications)
            throws IOException {
        CredentialsStore store= findUserStoreFirstOrNull(user);

        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store",
                    user.getId()));
        }

        Domain domain = findOrCreateDomain(store, domainName, domainSpecifications);

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

    /**
     * Get all domains this user has access to
     */
    public static @Nonnull Iterable<Domain> getUserDomains(@Nonnull User user){
        List<Domain> domains = new ArrayList<>();
        for(final CredentialsStore store: findUserStores(user)) {
            domains.addAll(store.getDomains());
        }
        for(final CredentialsStore store: CredentialsProvider.lookupStores(Jenkins.getInstance())){
            domains.addAll(store.getDomains());
        }
        return domains;
    }

    public static @CheckForNull <C extends Credentials> C findCredential(@Nonnull String credentialId, @Nonnull Class<C> type, @Nonnull DomainRequirement... domainRequirements){
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        type,
                        Jenkins.get(),
                        Jenkins.getAuthentication(),
                        domainRequirements),
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

    private static @Nonnull Iterable<CredentialsStore> findUserStores(User user){
        List<CredentialsStore> stores = new ArrayList<>();

        //First user store
        for (CredentialsStore store : CredentialsProvider.lookupStores(user)) {
            stores.add(store);
        }

        //then system store
        for (CredentialsStore store : CredentialsProvider.lookupStores(Jenkins.getInstance())) {
            stores.add(store);
        }
        return stores;

    }

    public static List<DomainSpecification> generateDomainSpecifications(@Nullable String  uriStr){
        if (StringUtils.isBlank(uriStr)) {
            return Collections.emptyList();
        }

        List<DomainSpecification> domainSpecifications = new ArrayList<>();
        try {
            URI uri = new URI(uriStr);

            // XXX: UriRequirementBuilder.fromUri() maps "" path to "/", so need to take care of it here
            String path = uri.getRawPath() == null ? null : (uri.getRawPath().trim().isEmpty() ? "/" : uri.getRawPath());

            domainSpecifications.add(new PathSpecification(path, "", false));
            if (uri.getPort() != -1) {
                domainSpecifications.add(new HostnamePortSpecification(uri.getHost() + ":" + uri.getPort(), null));
            } else {
                domainSpecifications.add(new HostnameSpecification(uri.getHost(), null));
            }
            domainSpecifications.add(new SchemeSpecification(uri.getScheme()));
        } catch (URISyntaxException e) {
            // TODO: handle git repo of form: [user@]host.xz:path/to/repo.git/, when URIRequirementBuilder.fromUri() supports it
            //       for now, we are returning empty list to match with  URIRequirementBuilder.fromUri()
            return domainSpecifications;
        }
        return Collections.emptyList();
    }

    private static @Nonnull Domain findOrCreateDomain(@Nonnull CredentialsStore store,
                                                      @Nonnull String domainName,
                                                      @Nonnull List<DomainSpecification> domainSpecifications)
            throws IOException {

        Domain domain = store.getDomainByName(domainName);
        if (domain == null) { //create new one
            boolean result = store.addDomain(new Domain(domainName,
                    domainName+" to store credentials by BlueOcean", domainSpecifications)
            );
            if (!result) {
                throw new ServiceException.BadRequestException("Failed to create credential domain: " + domainName);
            }
            domain = store.getDomainByName(domainName);
            if (domain == null) {
                throw new ServiceException.UnexpectedErrorException("Domain %s created but not found");
            }
        }
        return domain;
    }
}
