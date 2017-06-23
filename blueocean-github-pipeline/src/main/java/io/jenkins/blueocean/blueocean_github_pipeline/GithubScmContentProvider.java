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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Connector;
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
                    .withAuthorization("token " + scmParamsFromItem.accessToken)
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
        String apiUrl = GitHubSCMSource.GITHUB_URL;
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
                throw new ServiceException.BadRequestException("accessToken not found in pipeline: " + item.getFullName());
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
        private final String accessToken;

        public ScmParamsFromItem(Item item) {
            String apiUrl = null;
            String owner=null;
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
            this.apiUrl = apiUrl == null ? GitHubSCMSource.GITHUB_URL : apiUrl;

            if (credentialId != null) {
                StandardCredentials credentials = Connector.lookupScanCredentials((SCMSourceOwner) item, this.apiUrl, credentialId);
                if (credentials instanceof StandardUsernamePasswordCredentials) {
                    accessToken = ((StandardUsernamePasswordCredentials) credentials).getPassword().getPlainText();
                } else {
                    throw new ServiceException.BadRequestException("accessToken not found in pipeline: " + item.getFullName());
                }
            }
            if(owner == null){
                throw new ServiceException.BadRequestException(
                        String.format("Pipeline %s is not configured with github source correctly, no github user/org found", item.getFullName()));
            }
            if(accessToken == null){
                throw new ServiceException.BadRequestException(
                        String.format("Pipeline %s is not configured with github source correctly, no credentials with github accessToken found", item.getFullName()));
            }
            this.owner = owner;
            this.repo = repo;
            this.accessToken = accessToken;
        }
    }
}

