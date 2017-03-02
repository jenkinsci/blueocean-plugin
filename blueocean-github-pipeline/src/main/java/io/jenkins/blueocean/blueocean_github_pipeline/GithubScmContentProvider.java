package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -100)
public class GithubScmContentProvider extends ScmContentProvider {

    @Nonnull
    @Override
    public Object getContent(@Nonnull final SCMSource source, @Nonnull final SCMFile scmFile) {
        String data = content(scmFile);
        int size = data.length();

        String sha = sha(data);
        String base64Data = Base64.encodeBase64String(StringUtils.getBytesUtf8(data));

        return new GithubFile(new GithubContent.Builder()
                .base64Data(base64Data)
                .name(scmFile.getName())
                .path(scmFile.getPath())
                .owner(owner(source))
                .repo(repo(source))
                .sha(sha)
                .size(size)
                .build());
    }

    @Override
    public Object saveContent(@Nonnull StaplerRequest staplerRequest, @Nonnull Item item) {
        JSONObject body;
        try {
            body = JSONObject.fromObject(IOUtils.toString(staplerRequest.getReader()));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to read request body");
        }
        body.put("$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubScmSaveFileRequest");

        GithubScmSaveFileRequest request = staplerRequest.bindJSON(GithubScmSaveFileRequest.class, body);
        if(request == null){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to bind request"));
        }

        ScmContentProvider scmContentProvider = ScmContentProvider.resolve(item);

        if(scmContentProvider != null){
            return saveContent(request, item);
        }
        throw new ServiceException.BadRequestExpception("No save scm content provider found for pipeline: " + item.getFullName());
    }

    @SuppressWarnings("unchecked")
    private Object saveContent(@Nonnull GithubScmSaveFileRequest githubRequest, @Nonnull Item item) {
        String apiUrl = GithubScm.DEFAULT_API_URI;
        String owner = null;
        String repo = null;
        String accessToken = null;
        String credentialId = null;
        if (item instanceof OrganizationFolder) {
            List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
            if (!navigators.isEmpty() && navigators.get(0) instanceof GitHubSCMNavigator) {
                GitHubSCMNavigator navigator = (GitHubSCMNavigator) navigators.get(0);
                if (navigator.getApiUri() != null) {
                    apiUrl = navigator.getApiUri();
                }
                credentialId = navigator.getScanCredentialsId();
                owner = navigator.getRepoOwner();
            }
        } else if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            if (!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource) {
                GitHubSCMSource source = (GitHubSCMSource) sources.get(0);
                if (source.getApiUri() != null) {
                    apiUrl = source.getApiUri();
                }
                credentialId = source.getScanCredentialsId();
                owner = owner(source);
                repo = repo(source);
            }
        }
        if (credentialId != null) {
            StandardCredentials credentials = Connector.lookupScanCredentials((SCMSourceOwner) item, apiUrl, credentialId);
            if (credentials instanceof StandardUsernamePasswordCredentials) {
                accessToken = ((StandardUsernamePasswordCredentials) credentials).getPassword().getPlainText();
            } else {
                throw new ServiceException.BadRequestExpception("accessToken not found in pipeline: " + item.getFullName());
            }
        }
        return githubRequest.save(apiUrl, owner, repo, accessToken);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean support(@Nonnull Item item) {
        if (item instanceof OrganizationFolder) {
            List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
            return (!navigators.isEmpty() && navigators.get(0) instanceof GitHubSCMNavigator);
        } else if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            return (!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource);
        }
        return false;
    }

    //Gives file content as decoded string
    // SCMFile.contentAsString() returns decoded string, we just use it.
    private String content(SCMFile scmFile) {
        try {
            return scmFile.contentAsString();
        } catch (IOException | InterruptedException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to read file content: " + e.getMessage(), e);
        }
    }

    private String owner(SCMSource source) {
        if (source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepoOwner();
        }
        return null;
    }

    private String repo(SCMSource source) {
        if (source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepository();
        }
        return null;
    }

    //XXX: Hack till JENKINS-42270 gets address
    private String sha(String data) {
        return DigestUtils.sha1Hex("blob " + data.length() + "\0" + data);
    }
}

