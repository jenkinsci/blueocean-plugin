package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


@ExportedBean(defaultVisibility = 9999)
public class GithubEnterpriseScmCredential {

    private final StandardUsernamePasswordCredentials credential;

    public GithubEnterpriseScmCredential(StandardUsernamePasswordCredentials credential) {
        this.credential = credential;
    }

    @Exported
    public String getCredentialId() {
        return this.credential.getId();
    }

    @Exported
    public String getApiUri() {
        return getCredentialId().substring(getCredentialId().indexOf(":") + 1);
    }
}
