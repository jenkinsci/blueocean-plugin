package io.jenkins.blueocean.rest.impl.pipeline.credential;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;

import java.util.ArrayList;
import java.util.List;

/**
 * Credential search API
 * @author Vivek Pandey
 */
@Extension
public class
CredentialSearch extends OmniSearch<CredentialApi.Credential> {
    @Override
    public String getType() {
        return "credential";
    }

    @Override
    public Pageable<CredentialApi.Credential> search(Query q) {
        List<CredentialApi.Credential> credentials = new ArrayList<>();
        String domain = q.param("domain");
        String store = q.param("store");
        ExtensionList<CredentialContainer> extensionList = ExtensionList.lookup(CredentialContainer.class);
        if(!extensionList.isEmpty()) {
            CredentialContainer credentialContainer = extensionList.get(0);

            for (CredentialApi api : credentialContainer) {
                if(store != null && !store.equals(api.getStore())){
                    continue;
                }
                if (domain != null) {
                    CredentialApi.CredentialDomain d = api.getDomains().get(domain);
                    if (d == null) {
                        throw new ServiceException.BadRequestExpception("Credential domain " + domain + " not found");
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
        }

        return Pageables.wrap(credentials);
    }
}
