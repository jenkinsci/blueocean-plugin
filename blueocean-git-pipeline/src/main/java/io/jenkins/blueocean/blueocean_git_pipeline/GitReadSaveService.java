/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;

import java.io.IOException;
import javax.annotation.Nonnull;

import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Content provider for load/save with git repositories
 */
@Extension
public class GitReadSaveService extends ScmContentProvider {
    @Nonnull
    private static ReadSaveType TYPE = ReadSaveType.DEFAULT;

    /**
     * Type of git interaction to use
     */
    public enum ReadSaveType {
        CLONE,
        CACHE_CLONE,
        CACHE_BARE;

        static final ReadSaveType DEFAULT = get(System.getProperty("blueocean.features.GIT_READ_SAVE_TYPE"));

        static ReadSaveType get(String type) {
            if (type != null) {
                return ReadSaveType.valueOf(type.toUpperCase());
            }
            return DEFAULT == null ? ReadSaveType.CACHE_BARE : DEFAULT;
        }
    }

    @Nonnull
    @Override
    public String getScmId() {
        return "git";
    }

    @Override
    public String getApiUrl(@Nonnull Item item) {
        if (item instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
            MultiBranchProject<?,?> mbp = (MultiBranchProject<?,?>)item;
            return mbp.getSCMSources().stream()
                    .filter(s->s instanceof GitSCMSource)
                    .map(s -> ((GitSCMSource)s).getRemote())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static void setType(@Nonnull ReadSaveType type) {
        TYPE = type;
    }

    static GitReadSaveRequest makeSaveRequest(
        Item item, String branch, String commitMessage,
        String sourceBranch, String filePath, byte[] contents) {
        String defaultBranch = "master";
        GitSCMSource gitSource = null;
        if (item instanceof MultiBranchProject<?, ?>) {
            MultiBranchProject<?, ?> mbp = (MultiBranchProject<?, ?>) item;
            for (SCMSource s : mbp.getSCMSources()) {
                if (s instanceof GitSCMSource) {
                    gitSource = (GitSCMSource) s;
                }
            }
        }

        switch (TYPE) {
            case CLONE:
                return new GitCloneReadSaveRequest(
                    gitSource,
                    StringUtils.defaultIfEmpty(branch, defaultBranch),
                    commitMessage,
                    StringUtils.defaultIfEmpty(sourceBranch, defaultBranch),
                    filePath,
                    contents
                );
            case CACHE_CLONE:
                return new GitCacheCloneReadSaveRequest(
                    gitSource,
                    StringUtils.defaultIfEmpty(branch, defaultBranch),
                    commitMessage,
                    StringUtils.defaultIfEmpty(sourceBranch, defaultBranch),
                    filePath,
                    contents
                );
            default:
                return new GitBareRepoReadSaveRequest(
                    gitSource,
                    StringUtils.defaultIfEmpty(branch, defaultBranch),
                    commitMessage,
                    StringUtils.defaultIfEmpty(sourceBranch, defaultBranch),
                    filePath,
                    contents
                );
        }
    }

    private GitReadSaveRequest makeSaveRequest(Item item, StaplerRequest req) {
        System.out.println("makeSaveRequest(Item item, StaplerRequest req)"); // TODO:RM
        String branch = req.getParameter("branch");
        return makeSaveRequest(item,
                               branch,
                               req.getParameter("commitMessage"),
                               ObjectUtils.defaultIfNull(req.getParameter("sourceBranch"), branch),
                               req.getParameter("path"),
                               Base64.decode(req.getParameter("contents"))
        );
    }

    private GitReadSaveRequest makeSaveRequest(Item item, JSONObject json) {
        System.out.println("makeSaveRequest(Item item, JSONObject json)"); // TODO:RM
        JSONObject content = json.getJSONObject("content");
        String branch = content.getString("branch");
        return makeSaveRequest(item,
                               branch,
                               content.getString("message"),
                               content.has("sourceBranch") ? content.getString("sourceBranch") : branch,
                               content.getString("path"),
                               Base64.decode(content.getString("base64Data"))
        );
    }

    @Override
    public Object getContent(@Nonnull StaplerRequest req, @Nonnull Item item) {
        item.checkPermission(Item.READ);
        User user = User.current();
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Not authenticated");
        }

        GitReadSaveRequest r = makeSaveRequest(item, req);

        try {
            System.out.println("getContent - r is " + r); // TODO: RM
            System.out.println("           - about to read"); // TODO: RM

            final byte[] reqData = r.read();
            String encoded = Base64.encode(reqData);

            System.out.println("           - got " + (reqData == null ? "null" : reqData.length + " bytes")); // TODO: RM
            System.out.println("           - about to make gitcontent"); // TODO: RM

            final GitContent content = new GitContent(r.filePath, user.getId(), r.gitSource.getRemote(), r.filePath, 0, "sha", encoded, "", r.branch, r.sourceBranch, true, "");
            System.out.println("           - got " + content); // TODO: RM
            System.out.println("           - about to make gitfile"); // TODO: RM
            final GitFile gitFile = new GitFile(content);
            System.out.println("           - got " + gitFile + " so returning"); // TODO: RM
            return gitFile;
        } catch (ServiceException.UnauthorizedException e) {
            e.printStackTrace(); // TODO: RM
            throw new ServiceException.PreconditionRequired("Invalid credential", e);
        } catch (IOException e) {
            e.printStackTrace(); // TODO: RM
            throw new ServiceException.UnexpectedErrorException("Unable to get file content", e);
        }
    }

    @Override
    public Object saveContent(@Nonnull StaplerRequest req, @Nonnull Item item) {
        item.checkPermission(Item.CONFIGURE);
        User user = User.current();
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Not authenticated");
        }

        try {
            // parse json...
            JSONObject json = JSONObject.fromObject(IOUtils.toString(req.getReader()));
            GitReadSaveRequest r = makeSaveRequest(item, json);
            r.save();
            return new GitFile(
                new GitContent(r.filePath, user.getId(), r.gitSource.getRemote(), r.filePath, 0, "sha", null, "", r.branch, r.sourceBranch, true, "")
            );
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Unable to save file content", e);
        }
    }

    @Override
    public boolean support(@Nonnull Item item) {
        return getApiUrl(item) != null;
    }

    @Nonnull
    protected StandardUsernamePasswordCredentials getCredentialForUser(@Nonnull Item item, @Nonnull String apiUrl) {

        User user = User.current();
        if (user == null) { //ensure this session has authenticated user
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }

        String credentialId = GitScm.getCredentialId(apiUrl);
        StandardUsernamePasswordCredentials credential =
            CredentialsUtils.findCredential(credentialId,
                                            StandardUsernamePasswordCredentials.class,
                                            new BlueOceanDomainRequirement());

        System.out.println("GitScmParams - credential id is " + credentialId); // TODO: RM
        System.out.println("             - credential is " + credential); // TODO: RM

        if (credential == null) {
            throw new ServiceException.UnauthorizedException("No credential found for " + credentialId + " for user " + user.getDisplayName());
        }

        return credential;
    }

}
