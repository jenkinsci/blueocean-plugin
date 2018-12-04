package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub SCM save file API
 *
 * @author Vivek Pandey
 */
public class GithubScmSaveFileRequest{
    private final GitContent content;

    @DataBoundConstructor
    public GithubScmSaveFileRequest(GitContent content) {
        this.content = content;
    }

    public Object save(@Nonnull String apiUrl, @Nullable String owner, @Nullable String repoName, @Nullable String accessToken) {
        List<ErrorMessage.Error> errors = new ArrayList<>();

        if(this.content == null){
            errors.add(new ErrorMessage.Error("content",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "content is required parameter"));
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to save file to scm").addAll(errors));
        }else {
            errors.addAll(content.validate());
        }

        //if no owner given then check if its there in request
        if(owner == null){
            owner = content.getOwner();
        }
        if(repoName == null){
            repoName = content.getRepo();
        }

        if(owner == null){
            errors.add(new ErrorMessage.Error("content.owner",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "No scm owner found with pipeline %s, please provide content.owner parameter"));
        }
        if(repoName == null){
            errors.add(new ErrorMessage.Error("content.repo",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "No scm repo found with pipeline %s, please provide content.repo parameter"));
        }
        if(errors.size() > 0) {
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to save content").addAll(errors));
        }

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to save file to scm").addAll(errors));
        }

        try {
            GithubScm.validateUserHasPushPermission(apiUrl, accessToken, owner, repoName);

            String sha = createBranchIfNotPresent(apiUrl,owner,repoName,accessToken);

            //sha in request overrides the one we computed
            if(!StringUtils.isBlank(content.getSha())){
                sha = content.getSha();
            }

            Map<String,Object> body = new HashMap<>();
            body.put("message", content.getMessage());
            body.put("content", content.getBase64Data());

            if(!StringUtils.isBlank(content.getBranch())){
                body.put("branch", content.getBranch());
            }
            if(!StringUtils.isBlank(sha)){
                body.put("sha", sha);
            }
            final Map<?,?> ghResp = HttpRequest.put(String.format("%s/repos/%s/%s/contents/%s",
                    apiUrl,
                    owner,
                    repoName,
                    content.getPath()))
                    .withAuthorizationToken(accessToken)
                    .withBody(body)
                    .to(Map.class);

            if(ghResp == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file to Github: "+content.getPath());
            }

            final Map<?,?> ghContent = (Map<?,?>) ghResp.get("content");

            if(ghContent == null){
                throw new ServiceException.UnexpectedErrorException("Failed to save file: "+content.getPath());
            }

            return new GithubFile(new GitContent.Builder()
                    .sha((String)ghContent.get("sha"))
                    .name((String) ghContent.get("name"))
                    .repo(repoName)
                    .owner(owner)
                    .path((String) ghContent.get("path"))
                    .build());
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to save file: "+e.getMessage(), e);
        }
    }

    /**
     * Auto creates branch if:
     * <br>
     *  <li>content.branch is provided</li>
     *  <li>sha is not provided</li>
     *  <li>autoCreateBranch is false or null</li>
     *  <li>if its not empty repo</li>
     *
     *  @return If new branch is created, sha of content.path on the new branch otherwise null
     */
    private String createBranchIfNotPresent(String apiUrl, String owner, String repoName, String accessToken) throws IOException {
        //If no branch is provided or auto branch create flag is false then skip creation of branch
        if(StringUtils.isBlank(content.getBranch())
                || (content.isAutoCreateBranch() != null && !content.isAutoCreateBranch())){
            return null;
        }

        //If its empty repo, we can't create the branch on it. It gets created doing content creation
        if(isEmptyRepo(apiUrl,owner,repoName,accessToken)){
            return null;
        }
        //check if branch exists
        try {
            HttpRequest.head(String.format("%s/repos/%s/%s/branches/%s",
                    apiUrl,
                    owner,
                    repoName,
                    content.getBranch())).withAuthorizationToken(accessToken).to(String.class);
        } catch (ServiceException.NotFoundException e) {

            String sha = content.getSha();
            //branch doesn't exist, lets create new one
            //1. Find commit sha off which this branch will be created
            Map repo = HttpRequest.get(String.format("%s/repos/%s/%s", apiUrl,
                    owner, repoName))
                    .withAuthorizationToken(accessToken).to(Map.class);

            //2. Check the source branch or use default if not provided
            String sourceBranch = content.getSourceBranch();
            if (sourceBranch == null) {
                sourceBranch = (String) repo.get("default_branch");
            }

            //3. Get source branch's commit sha
            GithubBranch branch = HttpRequest.get(String.format("%s/repos/%s/%s/branches/%s",
                    apiUrl,
                    owner,
                    repoName,
                    sourceBranch)).withAuthorizationToken(accessToken).to(GithubBranch.class);

            //4. create this missing branch. We ignore the response, if no error branch was created
            HttpRequest.post(String.format("%s/repos/%s/%s/git/refs",
                    apiUrl,
                    owner,
                    repoName))
                    .withAuthorizationToken(accessToken)
                    .withBody(ImmutableMap.of("ref", "refs/heads/" + content.getBranch(),
                            "sha", branch.commit.sha))
                    .to(Map.class);

            //5. If request doesn't have sha get one from github
            //     Check and see if this path exists on this new branch, if it does,
            //     get its sha so that we can update this file
            if(StringUtils.isBlank(content.getSha())) {
                try {
                    GHContent ghContent = HttpRequest.get(String.format("%s/repos/%s/%s/contents/%s",
                            apiUrl,
                            owner,
                            repoName,
                            content.getPath()))
                            .withAuthorizationToken(accessToken)
                            .to(GHContent.class);
                    if (!StringUtils.isBlank(sha) && !sha.equals(ghContent.getSha())) {
                        throw new ServiceException.BadRequestException(String.format("sha in request: %s is different from sha of file %s in branch %s",
                                sha, content.getPath(), content.getBranch()));
                    }
                    return ghContent.getSha();
                } catch (ServiceException.NotFoundException e1) {
                    //not found, ignore it, we are good
                }
            }
        }
        return null;
    }

    private boolean isEmptyRepo(String apiUrl, String owner, String repoName, String accessToken) throws IOException {
        try {
            HttpRequest.head(String.format("%s/repos/%s/%s/contents",
                    apiUrl,
                    owner,
                    repoName)).withAuthorizationToken(accessToken).to(String.class);
        }catch (ServiceException.NotFoundException e){
            //its empty, return true
            return true;
        }
        return false;
    }

    //use it to deserialize github branch response
    private static class GithubBranch{
        private Commit commit;
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Used to deserialize JSON to Java")
        static class Commit{
            private String sha;
        }
    }
}
