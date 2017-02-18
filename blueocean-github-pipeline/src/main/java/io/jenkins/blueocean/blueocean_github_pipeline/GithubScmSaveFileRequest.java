package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmSaveFileRequest;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Github SCM save file API
 *
 * @author Vivek Pandey
 */
public class GithubScmSaveFileRequest extends ScmSaveFileRequest {
    private final Content content;
    private final String credentialId;

    @DataBoundConstructor
    public GithubScmSaveFileRequest(String credentialId, Content content) {
        this.credentialId = credentialId;
        this.content = content;
    }


    @Override
    public ScmFile save(Scm scm) {
        GithubScm.getAuthenticatedUser();
        if (!(scm instanceof GithubScm)) {
            throw new ServiceException.UnexpectedErrorException(String.format("Not a GitHub SCM: %s", scm.getClass().getName()));
        }
        GithubScm githubScm = (GithubScm) scm;

        List<ErrorMessage.Error> errors = new ArrayList<>();
        if(this.credentialId == null){
            errors.add(new ErrorMessage.Error("content.credentialId",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "credentialId is required parameter"));
        }

        StandardUsernamePasswordCredentials credential = CredentialsUtils.findCredential(githubScm.getId(), StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        if(credential == null){
            errors.add(new ErrorMessage.Error("content.credentialId",
                    ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(), "Invalid credentialId, no credentials found"));
        }
        String accessToken = credential.getPassword().getPlainText();

        if(this.content == null){
            errors.add(new ErrorMessage.Error("content",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "content is required parameter"));
        }else {
            errors.addAll(content.validate());
        }
        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to save file to scm").addAll(errors));
        }

        try {
            Map<String,Object> body = new HashMap<>();
            body.put("message", content.message);
            body.put("content", content.base64Data);

            if(!StringUtils.isBlank(content.branch)){
                body.put("branch", content.branch);
            }
            if(!StringUtils.isBlank(content.sha)){
                body.put("sha", content.sha);
            }
            final Map ghResp = HttpRequest.put(String.format("%s/repos/%s/%s/contents/%s",
                    githubScm.getUri(),
                    content.owner,
                    content.repo, content.path))
                    .withAuthorization("token "+accessToken)
                    .withBody(body)
                    .to(Map.class);

            if(ghResp == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file: "+content.path);
            }

            final Map ghContent = (Map) ghResp.get("content");

            if(ghContent == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file: "+content.path);
            }

            return new GithubScmFile(ghContent);

        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to save file: "+e.getMessage(), e);
        }
    }

    public static class Content{
        private final String owner;
        private final String repo;
        private final String path;
        private final String message;
        private final String base64Data;
        private final String branch;
        private final String sha;

        @DataBoundConstructor
        public Content(String owner, String repo, String path, String message, String base64Data, String  sha, String branch) {
            this.owner = owner;
            this.repo = repo;
            this.path = path;
            this.message = message;
            this.base64Data = base64Data;
            this.branch = branch;
            this.sha = sha;
        }

        private List<ErrorMessage.Error> validate(){
            List<ErrorMessage.Error> errors = new ArrayList<>();
            if(owner == null){
                errors.add(new ErrorMessage.Error("content.owner",
                        ErrorMessage.Error.ErrorCodes.MISSING.toString(), "owner is required parameter"));
            }
            if(repo == null){
                errors.add(new ErrorMessage.Error("content.repo",
                        ErrorMessage.Error.ErrorCodes.MISSING.toString(), "repo is required parameter"));
            }
            if(path == null){
                errors.add(new ErrorMessage.Error("content.path",
                        ErrorMessage.Error.ErrorCodes.MISSING.toString(), "path is required parameter"));
            }
            if(message == null){
                errors.add(new ErrorMessage.Error("content.message",
                        ErrorMessage.Error.ErrorCodes.MISSING.toString(), "message is required parameter"));
            }
            if(base64Data == null){
                errors.add(new ErrorMessage.Error("content.base64Data",
                        ErrorMessage.Error.ErrorCodes.MISSING.toString(), "base64Data is required parameter"));
            }
            return errors;
        }
    }

    public static class GithubScmFile extends ScmFile{
        private final String sha;

        private final String name;

        private final String path;

        private final Number size;

        private final String type;

        public GithubScmFile(Map ghContent) {
            this.sha = (String) ghContent.get("sha");
            this.name = (String) ghContent.get("name");
            this.path = (String) ghContent.get("path");
            this.size = (Number) ghContent.get("size");
            this.type =  (String) ghContent.get("type");
        }

        @Exported
        public String getSha() {
            return sha;
        }

        @Exported
        public String getName() {
            return name;
        }

        @Exported
        public String getPath() {
            return path;
        }

        @Exported
        public Number getSize() {
            return size;
        }

        @Exported
        public String getType() {
            return type;
        }

    }
}