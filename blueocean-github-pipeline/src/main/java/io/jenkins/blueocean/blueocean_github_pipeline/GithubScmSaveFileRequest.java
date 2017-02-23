package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmSaveFileRequest;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.stapler.DataBoundConstructor;

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
    public ScmContent save(Scm scm) {
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

            String sha = content.sha;
            //Lets check if this branch exists, if not then create it
            if(!StringUtils.isBlank(content.branch) && content.autoCreateBranch){
                try {
                    HttpRequest.head(String.format("%s/repos/%s/%s/branches/%s",
                            githubScm.getUri(),
                            content.owner,
                            content.repo,
                            content.branch)).withAuthorization("token "+accessToken).to(String.class);
                } catch (ServiceException.NotFoundException e) {
                    //branch doesn't exist, lets create new one

                    //1. Find commit sha off which this branch will be created
                    //2. We need to find default branch first
                    GHRepoEx repo = HttpRequest.get(String.format("%s/repos/%s/%s", githubScm.getUri(),
                            content.owner, content.repo))
                            .withAuthorization("token "+accessToken).to(GHRepoEx.class);

                    //3. Get default branch's commit sha
                    GHBranch branch = HttpRequest.get(String.format("%s/repos/%s/%s/branches/%s",
                            githubScm.getUri(),
                            content.owner,
                            content.repo,
                            repo.getDefaultBranch())).withAuthorization("token " + accessToken).to(GHBranch.class);

                    //4. create this missing branch. We ignore the response, if no error branch was created
                    HttpRequest.post(String.format("%s/repos/%s/%s/git/refs",
                            githubScm.getUri(),
                            content.owner,
                            content.repo))
                            .withAuthorization("token " + accessToken)
                            .withBody(ImmutableMap.of("ref", "refs/heads/" + content.branch,
                                    "sha", branch.getSHA1()))
                            .to(Map.class);

                    //5. Check and see if this path exists on this new branch, if it does,
                    //   get its sha so that we can update this file
                    try {
                        GHContent ghContent = HttpRequest.get(String.format("%s/repos/%s/%s/contents/%s",
                                githubScm.getUri(),
                                content.owner,
                                content.repo,
                                content.path))
                                .withAuthorization("token " + accessToken)
                                .to(GHContent.class);
                        if(!StringUtils.isBlank(content.sha) && !content.sha.equals(ghContent.getSha())){
                            throw new ServiceException.BadRequestExpception(String.format("sha in request: %s is different from sha of file %s in branch %s",
                                    content.sha, content.path, content.branch));
                        }
                        sha = ghContent.getSha();
                    }catch (ServiceException.NotFoundException e1){
                        //not found, ignore it, we are good
                    }
                }
            }

            Map<String,Object> body = new HashMap<>();
            body.put("message", content.message);
            body.put("content", content.base64Data);

            if(!StringUtils.isBlank(content.branch)){
                body.put("branch", content.branch);
            }
            if(!StringUtils.isBlank(sha)){
                body.put("sha", sha);
            }
            final Map ghResp = HttpRequest.put(String.format("%s/repos/%s/%s/contents/%s",
                    githubScm.getUri(),
                    content.owner,
                    content.repo,
                    content.path))
                    .withAuthorization("token "+accessToken)
                    .withBody(body)
                    .to(Map.class);

            if(ghResp == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file to Github: "+content.path);
            }

            final Map ghContent = (Map) ghResp.get("content");

            if(ghContent == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file: "+content.path);
            }

            return new GithubScmFileContent.Builder()
                    .sha((String)ghContent.get("sha"))
                    .name((String) ghContent.get("name"))
                    .repo(content.repo)
                    .owner(content.owner)
                    .path((String) ghContent.get("path"))
                    .build();
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
        private final boolean autoCreateBranch;

        @DataBoundConstructor
        public Content(String owner, String repo, String path, String message, String base64Data, String  sha, String branch, Boolean autoCreateBranch) {
            this.owner = owner;
            this.repo = repo;
            this.path = path;
            this.message = message;
            this.base64Data = base64Data;
            this.branch = branch;
            this.sha = sha;
            this.autoCreateBranch = autoCreateBranch == null ? true : autoCreateBranch;
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

}