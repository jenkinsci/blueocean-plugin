package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.ErrorMessage;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubScmContent {
    private final String owner;
    private final String repo;
    private final String path;
    private final String message;
    private final String base64Data;
    private final String branch;
    private final String sha;
    private final boolean autoCreateBranch;

    @DataBoundConstructor
    public GithubScmContent(String owner, String repo, String path, String message, String base64Data, String sha, String branch, Boolean autoCreateBranch) {
        this.owner = owner;
        this.repo = repo;
        this.path = path;
        this.message = message;
        this.base64Data = base64Data;
        this.branch = branch;
        this.sha = sha;
        this.autoCreateBranch = autoCreateBranch == null ? true : autoCreateBranch;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public String getBranch() {
        return branch;
    }

    public String getSha() {
        return sha;
    }

    public boolean isAutoCreateBranch() {
        return autoCreateBranch;
    }

    List<ErrorMessage.Error> validate() {
        List<ErrorMessage.Error> errors = new ArrayList<>();

        if (path == null) {
            errors.add(new ErrorMessage.Error("content.path",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "path is required parameter"));
        }
        if (message == null) {
            errors.add(new ErrorMessage.Error("content.message",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "message is required parameter"));
        }
        if (base64Data == null) {
            errors.add(new ErrorMessage.Error("content.base64Data",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "base64Data is required parameter"));
        }
        return errors;
    }

}
