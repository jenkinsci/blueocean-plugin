package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Preconditions;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContentProviderParams;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -100)
public class GithubScmContentProvider extends AbstractScmContentProvider {

    @Nonnull
    @Override
    public String getScmId() {
        return GithubScm.ID;
    }

    @Override
    public String getApiUrl(@Nonnull Item item) {
        if (item instanceof OrganizationFolder) {
            List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
            if ((!navigators.isEmpty() && navigators.get(0) instanceof GitHubSCMNavigator)) {
                return ((GitHubSCMNavigator) navigators.get(0)).getApiUri();
            }
        } else if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            if ((!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource)) {
                return ((GitHubSCMSource) sources.get(0)).getApiUri();
            }
        }

        return null;
    }

    @Override
    protected Object getContent(ScmGetRequest request) {
        GithubScm.validateUserHasPushPermission(request.getApiUrl(), request.getCredentials().getPassword().getPlainText(), request.getOwner(), request.getRepo());

        String url = String.format("%s/repos/%s/%s/contents/%s",
                request.getApiUrl(),
                request.getOwner(),
                request.getRepo(),
                request.getPath());
        if(request.getBranch() != null){ //if branch is present fetch this file from branch
            url += "?ref="+request.getBranch();
        }
        try {
            Map ghContent = HttpRequest.get(url)
                    .withAuthorizationToken(request.getCredentials().getPassword().getPlainText())
                    .to(Map.class);

            if(ghContent == null){
                throw new ServiceException.UnexpectedErrorException("Failed to load file: "+request.getPath());
            }

            String base64Data = (String)ghContent.get("content");
            // JENKINS-47887 - this content contains \n which breaks IE11
            base64Data = base64Data == null ? null : base64Data.replace("\n", "");
            return new GithubFile(new GitContent.Builder()
                    .sha((String)ghContent.get("sha"))
                    .name((String)ghContent.get("name"))
                    .repo(request.getRepo())
                    .owner(request.getOwner())
                    .path(request.getPath())
                    .base64Data(base64Data)
                    .build());
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(String.format("Failed to load file %s: %s", request.getPath(),e.getMessage()), e);
        }
    }

    @Override
    protected ScmContentProviderParams getScmParamsFromItem(Item item) {
        return new GithubScmParamsFromItem(item);
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
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to bind request"));
        }

        ScmContentProvider scmContentProvider = ScmContentProvider.resolve(item);

        if(scmContentProvider != null){
            return saveContent(request, item);
        }
        throw new ServiceException.BadRequestException("No save scm content provider found for pipeline: " + item.getFullName());
    }

    @SuppressWarnings("unchecked")
    private Object saveContent(@Nonnull GithubScmSaveFileRequest githubRequest, @Nonnull Item item) {
        GithubScmParamsFromItem scmParamsFromItem = new GithubScmParamsFromItem(item);
        String owner = scmParamsFromItem.getOwner();
        String repo = scmParamsFromItem.getRepo();
        String accessToken = scmParamsFromItem.getCredentials().getPassword().getPlainText();
        Preconditions.checkNotNull(scmParamsFromItem.getApiUrl(), String.format("Project %s is not setup with Github api URL", item.getFullName()));
        return githubRequest.save(scmParamsFromItem.getApiUrl(), owner, repo, accessToken);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean support(@Nonnull Item item) {
        if (isItemUsingGithubScm(item)) {
            String apiUrl = getApiUrl(item);

            if (apiUrl == null || apiUrl.startsWith(GitHubSCMSource.GITHUB_URL)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    protected boolean isItemUsingGithubScm(@Nonnull Item item) {
        if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            return (!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource);
        }
        return false;
    }

    private static class GithubScmParamsFromItem extends ScmContentProviderParams {
        private GithubScmParamsFromItem(Item item) {
            super(item);
            if(getCredentials() == null){
                throw new ServiceException.BadRequestException(
                        String.format("Pipeline %s is not configured with github source correctly, no credentials with github accessToken found", item.getFullName()));
            }
        }

        @Override
        protected String owner(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof GitHubSCMSource) {
                return ((GitHubSCMSource) scmSource).getRepoOwner();
            }
            return null;
        }

        @Override
        protected String owner(@Nonnull SCMNavigator scmNavigator) {
            if(scmNavigator instanceof GitHubSCMNavigator){
                return ((GitHubSCMNavigator)scmNavigator).getRepoOwner();
            }
            return null;

        }

        @Override
        protected String repo(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof GitHubSCMSource) {
                return ((GitHubSCMSource) scmSource).getRepository();
            }
            return null;
        }

        @Override
        protected String apiUrl(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof GitHubSCMSource) {
                return ((GitHubSCMSource) scmSource).getApiUri();
            }
            return null;
        }

        @Override
        protected String apiUrl(@Nonnull SCMNavigator scmNavigator) {
            if(scmNavigator instanceof GitHubSCMNavigator){
                return ((GitHubSCMNavigator)scmNavigator).getApiUri();
            }
            return null;
        }

        @Override
        protected StandardUsernamePasswordCredentials getCredentialForUser(final Item item, String apiUrl){
            User user = User.current();
            if(user == null){ //ensure this session has authenticated user
                throw new ServiceException.UnauthorizedException("No logged in user found");
            }

            StaplerRequest request = Stapler.getCurrentRequest();
            String scmId = request.getParameter("scmId");

            //get credential for this user
            GithubScm scm;
            final BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(item);
            if(apiUrl.startsWith(GitHubSCMSource.GITHUB_URL)
                    //tests might add scmId to indicate which Scm should be used to find credential
                    //We have to do this because apiUrl might be of WireMock server and not Github
                    || (StringUtils.isNotBlank(scmId) && scmId.equals(GithubScm.ID))) {
                scm = new GithubScm( () -> {
                    Preconditions.checkNotNull(organization);
                    return organization.getLink().rel("scm");
                } );
            }else{ //GHE
                scm = new GithubEnterpriseScm(( () -> {
                    Preconditions.checkNotNull(organization);
                    return organization.getLink().rel("scm");
                } ));
            }

            //pick up github credential from user's store
            StandardUsernamePasswordCredentials githubCredential = scm.getCredential(GithubScm.normalizeUrl(apiUrl));
            if(githubCredential == null){
                throw new ServiceException.PreconditionRequired("Can't access content from github: no credential found");
            }
            return githubCredential;
        }
    }
}

