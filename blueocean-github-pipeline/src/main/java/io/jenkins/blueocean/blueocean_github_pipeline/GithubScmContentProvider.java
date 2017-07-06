package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Preconditions;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -100)
public class GithubScmContentProvider extends ScmContentProvider {

    @Override
    public Object getContent(@Nonnull StaplerRequest request, @Nonnull Item item) {

        String path = StringUtils.defaultIfEmpty(request.getParameter("path"), null);
        String type = StringUtils.defaultIfEmpty(request.getParameter("type"), null);
        String repo = StringUtils.defaultIfEmpty(request.getParameter("repo"), null);
        String branch = StringUtils.defaultIfEmpty(request.getParameter("branch"),null);

        List<ErrorMessage.Error> errors = new ArrayList<>();

        if(!(item instanceof MultiBranchProject) && repo == null){
            errors.add(
                    new ErrorMessage.Error("repo",ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                            String.format("repo and branch parameters are required because pipeline %s is not a multi-branch project ",
                                    item.getFullName())));
        }

        if(type != null && !type.equals("file")){
            errors.add(
                    new ErrorMessage.Error("file",ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                            String.format("type %s not supported. Only 'file' type supported.", type)));
        }

        if(path == null){
            errors.add(
                    new ErrorMessage.Error("path",ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                            "path is required query parameter"));
        }

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(
                    new ErrorMessage(400, "Failed to load scm file").addAll(errors));
        }

        StandardUsernamePasswordCredentials credentials = getCredentialForUser(item);
        String accessToken = credentials.getPassword().getPlainText();
        ScmParamsFromItem scmParamsFromItem = new ScmParamsFromItem(item);

        //if no repo param, then see if its there in given Item.
        if(repo == null && scmParamsFromItem.repo == null){
            throw new ServiceException.BadRequestException("github repo could not be determine from pipeline: "+item.getFullName());
        }

        // If both, repo param and repo in pipeline scm configuration present, they better match
        if(repo != null && scmParamsFromItem.repo != null && !repo.equals(scmParamsFromItem.repo)){
            throw new ServiceException.BadRequestException(
                    String.format("repo parameter %s doesn't match with repo in pipeline %s github configuration repo: %s ",
                            repo, item.getFullName(), scmParamsFromItem.repo));
        }

        if(repo == null){
            repo = scmParamsFromItem.repo;
        }

        String url = String.format("%s/repos/%s/%s/contents/%s",
                scmParamsFromItem.apiUrl,
                scmParamsFromItem.owner,
                repo,
                path);
        if(branch != null){ //if branch is present fetch this file from branch
            url += "?ref="+branch;
        }
        try {
            Map ghContent = HttpRequest.get(url)
                    .withAuthorization("token " + accessToken)
                    .to(Map.class);

            if(ghContent == null){
                throw new ServiceException.UnexpectedErrorException("Failed to load file: "+path);
            }

            return new GithubFile(new GithubContent.Builder()
                    .sha((String)ghContent.get("sha"))
                    .name((String)ghContent.get("name"))
                    .repo(repo)
                    .owner(scmParamsFromItem.owner)
                    .path(path)
                    .base64Data((String)ghContent.get("content"))
                    .build());
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(String.format("Failed to load file %s: %s", path,e.getMessage()), e);
        }
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
        String apiUrl = GithubScm.DEFAULT_API_URI;
        String owner = null;
        String repo = null;
        if (item instanceof OrganizationFolder) {
            List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
            if (!navigators.isEmpty() && navigators.get(0) instanceof GitHubSCMNavigator) {
                GitHubSCMNavigator navigator = (GitHubSCMNavigator) navigators.get(0);
                String url = navigator.getApiUri();
                if (url != null) {
                    apiUrl = url;
                }
                owner = navigator.getRepoOwner();
            }
        } else if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            if (!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource) {
                GitHubSCMSource source = (GitHubSCMSource) sources.get(0);
                String url = source.getApiUri();
                if (url != null) {
                    apiUrl = url;
                }
                owner = owner(source);
                repo = repo(source);
            }
        }
        StandardUsernamePasswordCredentials credentials = getCredentialForUser(item);
        String accessToken = credentials.getPassword().getPlainText();
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

    private static String owner(SCMSource source) {
        if (source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepoOwner();
        }
        return null;
    }

    private static String repo(SCMSource source) {
        if (source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepository();
        }
        return null;
    }

    private static class ScmParamsFromItem {
        private final String apiUrl;
        private final String owner;
        private final String repo;

        public ScmParamsFromItem(Item item) {
            String apiUrl = null;
            String owner=null;
            String repo = null;
            if (item instanceof OrganizationFolder) {
                List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
                if (!navigators.isEmpty() && navigators.get(0) instanceof GitHubSCMNavigator) {
                    GitHubSCMNavigator navigator = (GitHubSCMNavigator) navigators.get(0);
                    if (navigator.getApiUri() != null) {
                        apiUrl = navigator.getApiUri();
                    }
                    owner = navigator.getRepoOwner();
                }
            } else if (item instanceof MultiBranchProject) {
                List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
                if (!sources.isEmpty() && sources.get(0) instanceof GitHubSCMSource) {
                    GitHubSCMSource source = (GitHubSCMSource) sources.get(0);
                    if (source.getApiUri() != null) {
                        apiUrl = source.getApiUri();
                    }
                    owner = owner(source);
                    repo = repo(source);
                }
            }
            this.apiUrl = apiUrl == null ? GithubScm.DEFAULT_API_URI : apiUrl;


            if(owner == null){
                throw new ServiceException.BadRequestException(
                        String.format("Pipeline %s is not configured with github source correctly, no github user/org found", item.getFullName()));
            }
            this.owner = owner;
            this.repo = repo;
        }
    }

    private StandardUsernamePasswordCredentials getCredentialForUser(final Item item){
        User user = User.current();
        if(user == null){ //ensure this session has authenticated user
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }
        //get credential for this user
        GithubScm scm = new GithubScm(new Reachable() {
            @Override
            public Link getLink() {
                BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(item);
                Preconditions.checkNotNull(organization);
                return organization.getLink().rel("scm");
            }
        });

        //pick up github credential from user's store
        StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(scm.getId(), StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        if(githubCredential == null){
            throw new ServiceException.PreconditionRequired("Can't access content from github: no credential found");
        }
        return githubCredential;
    }
}

