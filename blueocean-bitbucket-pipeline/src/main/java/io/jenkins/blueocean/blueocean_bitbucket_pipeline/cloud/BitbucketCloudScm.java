package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.base.Preconditions;
import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.AbstractBitbucketScm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudTeam;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudScm extends AbstractBitbucketScm {
    static final String DOMAIN_NAME="blueocean-bitbucket-cloud-domain";
    static final String ID = "bitbucket-cloud";
    static final String API_URL = "https://bitbucket.org/api/2.0/";

    public BitbucketCloudScm(Reachable parent) {
        super(parent);
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return null;
    }

    @Nonnull
    @Override
    protected String createCredentialId(@Nonnull String apiUrl) {
        return ID;
    }

    @Nonnull
    @Override
    protected String getDomainId() {
        return DOMAIN_NAME;
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        getAuthenticatedUser();

        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "This request must be made in HTTP context");

        String credentialId = getCredentialIdFromRequest(request);
        if(StringUtils.isNotBlank(credentialId)){
            return super.getOrganizations();
        }
        //connect case, no credentialId
        //TODO: somehow figure way to list credential from given credential Domain
        List<BbConnectCredential> credentials = CredentialsProvider.lookupCredentials(
                        BbConnectCredential.class,
                        Jenkins.getInstance(),
                        Jenkins.getAuthentication(),
                        new BlueOceanDomainRequirement());

        final List<ScmOrganization> orgs = new ArrayList<>();
        for (BbConnectCredential cred: credentials){
            String name = cred.getUsername();
            String displayName=cred.getTeamDisplayName();;
            String avatar = String.format("https://bitbucket.org/account/%s/avatar/50/", name);
            orgs.add(new BitbucketOrg(
                    new BbCloudTeam(name, displayName, avatar), new BitbucketCloudApi("https://bitbucket.org",cred), this.getLink()));
        }
        return new Container<ScmOrganization>() {
            @Override
            public ScmOrganization get(String name) {
                for(ScmOrganization organization: orgs){
                    if(organization instanceof BitbucketOrg){
                        if(((BitbucketOrg)organization).getKey().equals(name)){
                            return organization;
                        }
                    }
                }
                return null;
            }

            @Override
            public Link getLink() {
                return BitbucketCloudScm.this.getLink().rel("organizations");
            }

            @Override
            public Iterator<ScmOrganization> iterator() {
                return iterator(0, 100);
            }

            @Override
            public Iterator<ScmOrganization> iterator(int start, int limit) {
                if(limit <= 0){
                    limit = PagedResponse.DEFAULT_LIMIT;
                }
                if(start <0){
                    start = 0;
                }
                int page =  (start/limit) + 1;
                return orgs.iterator();
            }
        };
    }

        @Extension
    public static class BbScmFactory extends ScmFactory {
        @Override
        public Scm getScm(String id, Reachable parent) {
            if(id.equals(ID)){
                return getScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new BitbucketCloudScm(parent);
        }
    }
}
