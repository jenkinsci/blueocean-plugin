package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMProbeStat;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceEvent;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineCreateRequest extends AbstractPipelineCreateRequestImpl {

    private static final String DESCRIPTOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";
    private static final Logger logger = LoggerFactory.getLogger(GithubPipelineCreateRequest.class);

    private BlueScmConfig scmConfig;

    @DataBoundConstructor
    public GithubPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        this.scmConfig = scmConfig;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BluePipeline create(Reachable parent) throws IOException {

        String apiUrl = null;
        String orgName = getName(); //default
        String credentialId = null;
        StringBuilder sb = new StringBuilder();
        List<String> repos = new ArrayList<>();

        if (scmConfig != null) {
            apiUrl = StringUtils.defaultIfBlank(scmConfig.getUri(), GithubScm.DEFAULT_API_URI);
            if (scmConfig.getConfig().get("orgName") instanceof String) {
                orgName = (String) scmConfig.getConfig().get("orgName");
            }
            credentialId = scmConfig.getCredentialId();
            if (scmConfig != null && scmConfig.getConfig().get("repos") instanceof List) {
                for (String r : (List<String>) scmConfig.getConfig().get("repos")) {
                    sb.append(String.format("(%s\\b)?", r));
                    repos.add(r);
                }
            }
        }

        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("Must login to create a pipeline");
        }

        TopLevelItem item = null;
        try {
            if(credentialId != null) {
                validateCredentialId(credentialId, apiUrl);
            }
            item = create(Jenkins.getInstance(), getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);

            if (item instanceof OrganizationFolder) {
                if(credentialId != null) {
                    //Find domain attached to this credentialId, if present check if it's BlueOcean specific domain then
                    //add the properties otherwise simply use it
                    Domain domain = CredentialsUtils.findDomain(credentialId, authenticatedUser);
                    if(domain == null){ //this should not happen since validateCredentialId found the credential
                        throw new ServiceException.BadRequestExpception(
                                new ErrorMessage(400, "Failed to create pipeline")
                                        .add(new ErrorMessage.Error("scm.credentialId",
                                                ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                                                "No domain in user credentials found for credentialId: "+ scmConfig.getCredentialId())));
                    }
                    if(domain.test(new BlueOceanDomainRequirement())) {
                        ((OrganizationFolder) item)
                                .addProperty(
                                        new BlueOceanCredentialsProvider.FolderPropertyImpl(
                                                authenticatedUser.getId(), credentialId,
                                                BlueOceanCredentialsProvider.createDomain(apiUrl)
                                        ));
                    }
                }
                GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);
                if (sb.length() > 0) {
                    gitHubSCMNavigator.setPattern(sb.toString());
                }

                // cick of github scan build
                OrganizationFolder organizationFolder = (OrganizationFolder) item;
                organizationFolder.getNavigators().replace(gitHubSCMNavigator);

                GithubOrganizationFolder githubOrganizationFolder = new GithubOrganizationFolder(organizationFolder, parent.getLink());
                if(repos.size() == 1){

                    final boolean hasJenkinsfile = repoHasJenkinsFile(apiUrl,credentialId, orgName, repos.get(0));
                    if(hasJenkinsfile){
                        SCMSourceEvent.fireNow(new SCMSourceEventImpl(repos.get(0), item, apiUrl, gitHubSCMNavigator));
                    }
                    githubOrganizationFolder.addRepo(repos.get(0), new GithubOrganizationFolder.BlueRepositoryProperty(){
                        @Override
                        public boolean meetsIndexingCriteria() {
                            return hasJenkinsfile;
                        }
                    });
                }else {
                    organizationFolder.scheduleBuild(new Cause.UserIdCause());
                }
                return githubOrganizationFolder;
            }
        } catch (Exception e){
            String msg = String.format("Error creating pipeline %s: %s",getName(),e.getMessage());
            logger.error(msg, e);
            if(item != null) {
                try {
                    item.delete();
                } catch (InterruptedException e1) {
                    logger.error(String.format("Error creating pipeline %s: %s",getName(),e1.getMessage()), e1);
                    throw new ServiceException.UnexpectedErrorException("Error cleaning up pipeline " + getName() + " due to error: " + e.getMessage(), e);
                }
            }
            if(e instanceof ServiceException){
                throw (ServiceException)e;
            }
            throw new ServiceException.UnexpectedErrorException(msg, e);
        }
        return null;
    }

    static boolean repoHasJenkinsFile(String apiUrl, String credentialId, String owner, String repo) throws IOException, InterruptedException {
        GitHubSCMSource gitHubSCMSource = new GitHubSCMSource(null, apiUrl, credentialId, credentialId, owner, repo);

        final JenkinsfileCriteria criteria = new JenkinsfileCriteria();
        gitHubSCMSource.fetch(criteria, new SCMHeadObserver() {
            @Override
            public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) throws IOException, InterruptedException {
                //do nothing
            }

            @Override
            public boolean isObserving() {
                //if jenkinsfile is found stop observing
                return !criteria.isJekinsfileFound();

            }
        }, TaskListener.NULL);

        return criteria.isJekinsfileFound();
    }

    static void validateCredentialId(String credentialId,  String apiUrl) throws IOException {
        if (credentialId != null && !credentialId.trim().isEmpty()) {
            StandardUsernamePasswordCredentials credentials = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
            if (credentials == null) {
                throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Github pipeline")
                        .add(new ErrorMessage.Error("scmConfig.credentialId",
                                ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                                "No Credentials instance found for credentialId: "+credentialId)));
            } else {
                String accessToken = credentials.getPassword().getPlainText();
                validateGithubAccessToken(accessToken, apiUrl);
            }
        }
    }

    private static void deleteOnError(AbstractFolder item){
        try {
            item.delete();
        } catch (InterruptedException | IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failure during cleaning up folder: " + item.getName() + ". Error: " +
                    e.getMessage(), e);
        }

    }

    private static void validateGithubAccessToken(String accessToken, String apiUrl) throws IOException {
        try {
            HttpURLConnection connection =  GithubScm.connect(apiUrl+"/user", accessToken);
            GithubScm.validateAccessTokenScopes(connection);
        } catch (Exception e) {
            if(e instanceof ServiceException){
                throw e;
            }
            throw new ServiceException.UnexpectedErrorException("Failure validating github access token: "+e.getMessage(), e);
        }
    }

    static class SCMSourceEventImpl extends SCMSourceEvent<Object>{
        private final String repoName;
        private final Item project;
        private final GitHubSCMNavigator navigator;

        public SCMSourceEventImpl(String repoName, Item project, String origin, GitHubSCMNavigator navigator) {
            super(Type.CREATED, new Object(), origin);
            this.repoName = repoName;
            this.project=project;
            this.navigator = navigator;
        }

        @Override
        public boolean isMatch(@NonNull SCMNavigator navigator) {
            return this.navigator == navigator;
        }

        @Override
        public boolean isMatch(@NonNull SCMSource source) {
            return ((GitHubSCMSource)source).getRepository().equals(getSourceName()) &&
                    source.getOwner().getFullName().equals(project.getFullName());
        }

        @NonNull
        @Override
        public String getSourceName() {
            return repoName;
        }
    }

    private static class JenkinsfileCriteria implements SCMSourceCriteria{
        private AtomicBoolean jenkinsFileFound = new AtomicBoolean();

            @Override
            public boolean isHead(@NonNull Probe probe, @NonNull TaskListener listener) throws IOException {
                    SCMProbeStat stat = probe.stat("Jenkinsfile");
                    boolean foundJekinsFile =  stat.getType() != SCMFile.Type.NONEXISTENT && stat.getType() != SCMFile.Type.DIRECTORY;
                    if(foundJekinsFile && !jenkinsFileFound.get()) {
                        jenkinsFileFound.set(true);
                    }
                    return foundJekinsFile;
            }

            private boolean isJekinsfileFound() {
                return jenkinsFileFound.get();
            }
        }
}
