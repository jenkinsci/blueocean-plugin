package io.jenkins.blueocean.rest.impl.pipeline.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Item;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import jenkins.branch.MultiBranchProject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractScmContentProvider extends ScmContentProvider {

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

        ScmContentProviderParams scmParamsFromItem = getScmParamsFromItem(item);

        //if no repo param, then see if its there in given Item.
        if(repo == null && scmParamsFromItem.getRepo() == null){
            throw new ServiceException.BadRequestException("github repo could not be determine from pipeline: "+item.getFullName());
        }

        // If both, repo param and repo in pipeline scm configuration present, they better match
        if(repo != null && scmParamsFromItem.getRepo() != null && !repo.equals(scmParamsFromItem.getRepo())){
            throw new ServiceException.BadRequestException(
                    String.format("repo parameter %s doesn't match with repo in pipeline %s github configuration repo: %s ",
                            repo, item.getFullName(), scmParamsFromItem.getRepo()));
        }

        if(repo == null){
            repo = scmParamsFromItem.getRepo();
        }

        ScmGetRequest scmGetRequest = new ScmGetRequest.Builder(scmParamsFromItem.getApiUrl())
                .branch(branch)
                .owner(scmParamsFromItem.getOwner())
                .repo(repo)
                .branch(branch)
                .path(path)
                .credentials(scmParamsFromItem.getCredentials()).build();

        return getContent(scmGetRequest);
    }

    protected abstract Object getContent(ScmGetRequest request);

    protected abstract ScmContentProviderParams getScmParamsFromItem(Item item);

    public static class ScmGetRequest{
        private final String apiUrl;
        private final String owner;
        private final String repo;
        private final String branch;
        private final String path;
        private final String type;
        private final StandardUsernamePasswordCredentials credentials;

        private ScmGetRequest(@Nonnull String apiUrl,
                              @Nonnull String owner,
                              @Nonnull String repo,
                              @Nullable String branch,
                              @Nonnull String path,
                              @Nullable String type,
                              @Nonnull StandardUsernamePasswordCredentials credentials) {
            this.apiUrl = apiUrl;
            this.owner = owner;
            this.repo = repo;
            this.branch = branch;
            this.path = path;
            this.type = type;
            this.credentials = credentials;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public String getOwner() {
            return owner;
        }

        public String getRepo() {
            return repo;
        }

        public String getBranch() {
            return branch;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public StandardUsernamePasswordCredentials getCredentials() {
            return credentials;
        }

        public static class Builder{
            private String apiUrl;
            private String owner;
            private String repo;
            private String branch;
            private String path;
            private String type;
            private StandardUsernamePasswordCredentials credentials;

            public Builder(String apiUrl) {
                this.apiUrl = apiUrl;
            }

            Builder owner(String owner){
                this.owner = owner;
                return this;
            }

            Builder repo(String repo){
                this.repo = repo;
                return this;
            }

            Builder path(String path){
                this.path = path;
                return this;
            }

            Builder branch(String branch){
                this.branch = branch;
                return this;
            }

            Builder type(String type){
                this.type = type;
                return this;
            }

            Builder credentials(StandardUsernamePasswordCredentials credentials){
                this.credentials = credentials;
                return this;
            }

            public ScmGetRequest build(){
                return new ScmGetRequest(apiUrl,owner,repo,branch,path, type, credentials);
            }

        }

    }
}
