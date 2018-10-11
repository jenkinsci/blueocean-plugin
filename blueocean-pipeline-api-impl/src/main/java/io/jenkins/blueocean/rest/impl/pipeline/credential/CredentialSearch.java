package io.jenkins.blueocean.rest.impl.pipeline.credential;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;

import java.util.ArrayList;
import java.util.List;

/**
 * Credential search API
 * @author Vivek Pandey
 */
@Extension
public class CredentialSearch extends OmniSearch<CredentialApi.Credential> {
    @Override
    public String getType() {
        return "credential";
    }

    @Override
    public Pageable<CredentialApi.Credential> search(Query q) {
        List<CredentialApi.Credential> credentials = new ArrayList<>();
        String domain = q.param("domain");
        String store = q.param("store");
        BlueOrganization organization = getOrganization(q);
        CredentialContainer credentialContainer = new CredentialContainer(organization.getLink());
        for (CredentialApi api : credentialContainer) {
            if(store != null && !store.equals(api.getStore())){
                continue;
            }
            if (domain != null) {
                CredentialApi.CredentialDomain d = api.getDomains().get(domain);
                if (d == null) {
                    throw new ServiceException.BadRequestException("Credential domain " + domain + " not found");
                }
                for (CredentialApi.Credential c : d.getCredentials()) {
                    credentials.add(c);
                }
            } else {
                for (CredentialApi.CredentialDomain d : api.getDomains()) {
                    for (CredentialApi.Credential c : d.getCredentials()) {
                        credentials.add(c);
                    }
                }
            }
        }
        return Pageables.wrap(credentials);
    }

    private BlueOrganization getOrganization(Query q){
        String org = q.param("organization");
        if(org == null){
            throw new ServiceException.BadRequestException("Credentials search query parameter 'organization' is required");
        }

        BlueOrganization organization = OrganizationFactory.getInstance().get(org);
        if(organization == null){
            throw new ServiceException.BadRequestException(
                    String.format("Organization %s not found. Query parameter 'organization' value: %s is invalid. ", org,org));
        }
        return organization;
    }

}
