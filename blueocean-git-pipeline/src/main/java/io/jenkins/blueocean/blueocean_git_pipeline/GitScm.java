package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.User;
import hudson.util.HttpResponses;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import io.jenkins.blueocean.rest.model.Container;
import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import java.util.List;

public class GitScm extends AbstractScm {
    public static final String ID = "git";

    protected final Reachable parent;

    public GitScm(Reachable parent) {
        this.parent = parent;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("git");
    }

    @Override
    @Nonnull
    public String getId() {
        return ID;
    }

    @Override
    @Nonnull
    public String getUri() {
        return "";
    }

    @Override
    public String getCredentialId() {
        return null;
    }

    @Override
    public Object getState() {
        return super.getState();
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        return null;
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return null;
    }

    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {
        String repositoryUrl;
        if (request.has("repositoryUrl")) {
            repositoryUrl = request.getString("repositoryUrl");
        } else {
            try {
                String fullName = request.getJSONObject("pipeline").getString("fullName");
                SCMSourceOwner item = Jenkins.getInstance().getItemByFullName(fullName, SCMSourceOwner.class);
                if (item != null) {
                    AbstractGitSCMSource scmSource = (AbstractGitSCMSource) item.getSCMSources().iterator().next();
                    repositoryUrl = scmSource.getRemote();
                }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        String credentialId = request.getString("credentialId");
        User user = User.current();
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Not authenticated");
        }
        StandardCredentials creds = CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(
                StandardCredentials.class,
                Jenkins.getInstance(),
                Jenkins.getAuthentication(),
                (List<DomainRequirement>)null),
            CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId))
        );

        if (creds == null) {
            throw new ServiceException.NotFoundException("No credentials found for: " + credentialId);
        }

        List<ErrorMessage.Error> errors = GitUtils.validateCredentials(repositoryUrl, creds);
        if (!errors.isEmpty()) {
            throw new ServiceException.UnauthorizedException(errors.get(0).getMessage());
        }

        return HttpResponses.okJSON();
    }

    @Extension
    public static class GitScmFactory extends ScmFactory {
        @Override
        public Scm getScm(@Nonnull String id, @Nonnull Reachable parent) {
            if(id.equals(ID)){
                return new GitScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new GitScm(parent);
        }
    }
}
