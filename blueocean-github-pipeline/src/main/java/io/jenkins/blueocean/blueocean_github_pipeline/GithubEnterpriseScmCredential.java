package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


@ExportedBean(defaultVisibility = 9999)
public class GithubEnterpriseScmCredential {

    private final StandardUsernamePasswordCredentials credential;

    public GithubEnterpriseScmCredential(StandardUsernamePasswordCredentials credential) {
        this.credential = credential;
    }

    @Exported(name = "id")
    public String getId() {
        return this.credential.getId();
    }

    @Exported(name = "apiUri")
    public String getApiUri() {
        return getId().substring(getId().indexOf(":") + 1);
        /*
        try {
            return URIUtil.decode(encodedUri);
        } catch (URIException e) {
            return "";
        }
        */
    }
}
