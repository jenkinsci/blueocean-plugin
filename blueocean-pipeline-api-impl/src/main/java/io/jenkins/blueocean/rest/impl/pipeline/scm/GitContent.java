package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.commons.ErrorMessage;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GitContent extends ScmContent {
    private final String name;
    private final String owner;
    private final String repo;
    private final String path;
    private final String message;
    private final String base64Data;
    private final String branch;
    private final String sha;
    private final String sourceBranch;
    private final Boolean autoCreateBranch;
    private final Number size;
    private final String commitId;


    @DataBoundConstructor
    public GitContent(String owner, String repo, String path, String message, String base64Data, String sha, String branch, String sourceBranch, Boolean autoCreateBranch, String commitId) {
        this(null, owner, repo, path, 0, message, base64Data, sha, branch, sourceBranch, autoCreateBranch, commitId);
    }

    public GitContent(String name, String owner, String repo, String path, Number size, String message, String base64Data, String sha, String branch, String sourceBranch, Boolean autoCreateBranch, String commitId) {
        this.name = name;
        this.owner = owner;
        this.repo = repo;
        this.path = path;
        this.message = message;
        this.base64Data = base64Data;
        this.branch = branch;
        this.sha = sha;
        this.sourceBranch = sourceBranch;
        this.autoCreateBranch = autoCreateBranch;
        this.size = size;
        this.commitId = commitId;
    }

    @Exported(name = "name")
    public String getName() {
        return name;
    }

    @Exported(name = "owner")
    public String getOwner() {
        return owner;
    }

    @Exported(name = "repo")
    public String getRepo() {
        return repo;
    }

    @Exported(name = "path")
    public String getPath() {
        return path;
    }

    @Exported(name = "size", skipNull = true)
    public Number getSize() {
        return size;
    }

    @Exported(name = "message", skipNull = true)
    public String getMessage() {
        return message;
    }

    @Exported(name = "base64Data", skipNull = true)
    public String getBase64Data() {
        return base64Data;
    }

    @Exported(name = "branch", skipNull = true)
    public String getBranch() {
        return branch;
    }

    @Exported(name = "sha")
    public String getSha() {
        return sha;
    }

    @Exported(name = "sourceBranch", skipNull = true)
    public String getSourceBranch() {
        return sourceBranch;
    }

    @Exported(name = "autoCreateBranch", skipNull = true)
    public Boolean isAutoCreateBranch() {
        return autoCreateBranch;
    }

    @Exported(name = "commitId", skipNull = true)
    public String getCommitId() {
        return commitId;
    }

    public List<ErrorMessage.Error> validate() {
        List<ErrorMessage.Error> errors = new ArrayList<>();

        if (path == null) {
            errors.add(new ErrorMessage.Error("content.path",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "path is required parameter"));
        }
        if (path == null) {
            errors.add(new ErrorMessage.Error("content.message",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "message is required parameter"));
        }
        if (path == null) {
            errors.add(new ErrorMessage.Error("content.base64Data",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(), "base64Data is required parameter"));
        }
        return errors;
    }

    //convenience builder
    public static class Builder {
        private String name;
        private String owner;
        private String repo;
        private String path;
        private String message;
        private String base64Data;
        private String branch;
        private String sha;
        private String sourceBranch;
        private Boolean autoCreateBranch;
        private Number size;
        private String commitId;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder base64Data(String base64Data) {
            this.base64Data = base64Data;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder sha(String sha) {
            this.sha = sha;
            return this;
        }

        public Builder sourceBranch(String sourceBranch) {
            this.sourceBranch = sourceBranch;
            return this;
        }

        public Builder autoCreateBranch(Boolean autoCreateBranch) {
            this.autoCreateBranch = autoCreateBranch;
            return this;
        }

        public Builder size(Number size) {
            this.size = size;
            return this;
        }

        public Builder commitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public GitContent build() {
            return new GitContent(name, owner, repo, path, size, message, base64Data, sha, branch, sourceBranch, autoCreateBranch, commitId);
        }
    }
}
