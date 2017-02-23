package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContentProvider;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -100)
public class GithubScmContentProvider extends ScmContentProvider {

    @Nonnull
    @Override
    public ScmContent getContent(@Nonnull final SCMSource source, @Nonnull final SCMFile scmFile) {
        String data = content(scmFile);
        int size = data.length();

        String sha;

        // Encode only if not base64 encoded
        if(!Base64.isBase64(data)){
            sha =  sha(data);
            data = Base64.encodeBase64String(StringUtils.getBytesUtf8(data));
        }else{
            //XXX: its base64 encoded, to calculate we need to decode it first
            //     This should go away once we have sha and encoded content available from upstream
            sha =  sha(new String(Base64.decodeBase64(data)));
        }

        return new GithubScmFileContent.Builder()
                .encodedContent(data)
                .encoding("base64")
                .name(scmFile.getName())
                .path(scmFile.getPath())
                .owner(owner(source))
                .repo(repo(source))
                .sha(sha)
                .size(size)
                .build();

    }

    @Override
    public boolean support(@Nonnull SCMSource source) {
        return source instanceof GitHubSCMSource;
    }

    private String content(SCMFile scmFile){
        try {
            return scmFile.contentAsString();
        } catch (IOException | InterruptedException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to read file content: "+e.getMessage(), e);
        }
    }

    private String owner(SCMSource source){
        if(source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepoOwner();
        }
        return null;
    }

    private String repo(SCMSource source){
        if(source instanceof GitHubSCMSource) {
            GitHubSCMSource githubSCMSource = (GitHubSCMSource) source;
            return githubSCMSource.getRepository();
        }
        return null;
    }

    //XXX: Hack till JENKINS-42270 gets address
    private String sha(String data){
        return DigestUtils.sha1Hex("blob " + data.length() + "\0" + data);
    }
}

