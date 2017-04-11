package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.base.Preconditions;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.MultiBranchProject;
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
        Preconditions.checkNotNull(parent, "Parent passed is null");
        Preconditions.checkNotNull(getName(), "Name provided was null");

        final List<String> repos = getRepositories();
        final String repositoryToAdd = repos.size() == 1 ? repos.get(0) : null;
        final boolean isUpdatingSingleRepo = repositoryToAdd != null;

        Item item = Jenkins.getInstance().getItemByFullName(getName());
        boolean creatingNewItem = item == null;
        if (item == null) {
            item = create(Jenkins.getInstance(), getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);
        }

        // If the item found is not an org folder then bail out
        if (!(item instanceof OrganizationFolder)) {
            // If this is a new item
            if (creatingNewItem) {
                try {
                    item.delete();
                } catch (InterruptedException e) {
                    throw new UnexpectedErrorException("could not delete item", e);
                }
            }
            throw new UnexpectedErrorException("item created is not an organization folder");
        }

        final String validatedCredentialId = getAndValidateCredentialId(item);

        try {
            // kick of github scan build
            OrganizationFolder organizationFolder = (OrganizationFolder) item;

            GitHubSCMNavigator navigator = updateNavigator(repos, organizationFolder, validatedCredentialId, isUpdatingSingleRepo);

            GithubOrganizationFolder githubOrganizationFolder = new GithubOrganizationFolder(organizationFolder, parent.getLink());
            if(isUpdatingSingleRepo){
                String apiUrl = getApiUrl();
                final boolean hasJenkinsfile = repoHasJenkinsFile(apiUrl, validatedCredentialId, getOrganizationName(), repositoryToAdd, organizationFolder);
                if(hasJenkinsfile){
                    // Fire off a scan and let the UI know when has completed
                    SCMSourceEvent.fireNow(new SCMSourceEventImpl(repositoryToAdd, item, apiUrl, navigator));
                    SSEEvents.sendMultibranchIndexingCompleteEvent(item, organizationFolder, repositoryToAdd, 5);
                } else {
                    // Let the UI know that we have created the org folder
                    SSEEvents.sendOrganizationScanCompleteEvent(item, organizationFolder);
                }
                // Add the new repository to the folder
                githubOrganizationFolder.addRepo(repositoryToAdd, new GithubOrganizationFolder.BlueRepositoryProperty(){
                    @Override
                    public boolean meetsIndexingCriteria() {
                        return hasJenkinsfile;
                    }
                });
            } else {
                organizationFolder.scheduleBuild(new Cause.UserIdCause());
            }
            organizationFolder.save();
            return githubOrganizationFolder;
        } catch (Exception e){
            return cleanupOnError(e, getName(), item, creatingNewItem);
        }
    }

    GitHubSCMNavigator updateNavigator(List<String> repos, OrganizationFolder organizationFolder, String validatedCredentialId, boolean isUpdatingSingleRepo) throws IOException {
        GitHubSCMNavigator gitHubSCMNavigator;
        if (isUpdatingSingleRepo) {
            // Create new navigator
            gitHubSCMNavigator = new GitHubSCMNavigator(getApiUrl(), getOrganizationName(), validatedCredentialId, validatedCredentialId);

            // currently, we are setting a series of regular expressions to match the repositories
            // so we need to extract the current set for incoming create requests to keep them
            // see a few lines below for the pattern being used
            Matcher matcher = Pattern.compile("\\((.*?)\\\\b\\)\\?").matcher(gitHubSCMNavigator.getPattern());
            while (matcher.find()) {
                String existingRepo = matcher.group(1);
                if (!repos.contains(existingRepo)) {
                    repos.add(existingRepo);
                }
            }
            // Add any existing discovered repos
            for (MultiBranchProject<?,?> p : organizationFolder.getItems()) {
                if (!repos.contains(p.getName())) {
                    repos.add(p.getName());
                }
            }

            // Add all the collected repositories to the navigator pattern
            StringBuilder sb = new StringBuilder();
            for (String r : repos) {
                sb.append(String.format("(%s\\b)?", r));
            }
            if (sb.length() > 0) {
                gitHubSCMNavigator.setPattern(sb.toString());
            }

            // Replace the old navigator with our new one
            organizationFolder.getNavigators().replace(gitHubSCMNavigator);

        } else {
            gitHubSCMNavigator = organizationFolder.getNavigators().get(GitHubSCMNavigator.class);
            gitHubSCMNavigator.setPattern(".*");
        }
        return gitHubSCMNavigator;
    }

    /**
     * Throws the correct exception for the REST API when there is an error.
     * Removes the item if this process was creating the item.
     * @param e exception
     * @param item being created or updated
     * @param creatingNewItem if this item is a new item created by this process or not
     * @return created pipeline
     * @throws IOException if the item has failed deletion
     */
    static BluePipeline cleanupOnError(Exception e, String name, Item item, boolean creatingNewItem) throws IOException {
        String msg = String.format("Error creating pipeline %s: %s",name,e.getMessage());
        logger.error(msg, e);
        if(item != null && creatingNewItem) {
            try {
                item.delete();
            } catch (InterruptedException e1) {
                logger.error(String.format("Error creating pipeline %s: %s",name,e1.getMessage()), e1);
                throw new UnexpectedErrorException("Error cleaning up pipeline " + name + " due to error: " + e.getMessage(), e);
            }
        }
        if(e instanceof ServiceException){
            throw (ServiceException)e;
        }
        throw new UnexpectedErrorException(msg, e);
    }

    private static boolean repoHasJenkinsFile(String apiUrl, String credentialId, String owner, String repo, OrganizationFolder sourceOwner) throws IOException, InterruptedException {
        GitHubSCMSource gitHubSCMSource = new GitHubSCMSource(null, apiUrl, credentialId, credentialId, owner, repo);

        // set OrganizationFolder as owner so that credentials attached to this org folder can be used during github api calls.
        gitHubSCMSource.setOwner(sourceOwner);

        final JenkinsfileCriteria criteria = new JenkinsfileCriteria();
        gitHubSCMSource.fetch(criteria, new SCMHeadObserver() {
            @Override
            public void observe(@Nonnull SCMHead head, @Nonnull SCMRevision revision) throws IOException, InterruptedException {
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

    private String getAndValidateCredentialId(Item item) {
        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("Must login to create a pipeline");
        }

        String apiUrl = getApiUrl();
        String credentialId = getCredentialsId(item);

        // If credential provided, validate it or throw error to fail
        if(isNotEmpty(credentialId) && isNotEmpty(apiUrl)) {
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
                try {
                    ((OrganizationFolder) item)
                        .addProperty(
                            new BlueOceanCredentialsProvider.FolderPropertyImpl(
                                authenticatedUser.getId(), credentialId,
                                BlueOceanCredentialsProvider.createDomain(apiUrl)
                            ));
                } catch (IOException e) {
                    throw new UnexpectedErrorException(e.getMessage(), e);
                }
            }
        }

        return credentialId;
    }

    private static void validateGithubAccessToken(String accessToken, String apiUrl) throws ServiceException {
        try {
            HttpURLConnection connection =  GithubScm.connect(apiUrl+"/user", accessToken);
            GithubScm.validateAccessTokenScopes(connection);
        } catch (Exception e) {
            throw new UnexpectedErrorException("Failure validating github access token: "+e.getMessage(), e);
        }
    }

    private String getApiUrl() {
        return scmConfig != null ? StringUtils.defaultIfBlank(scmConfig.getUri(), GithubScm.DEFAULT_API_URI) : null;
    }

    private String getOrganizationName() {
        if (scmConfig != null && scmConfig.getConfig().get("orgName") instanceof String) {
            return (String) scmConfig.getConfig().get("orgName");
        }
        return getName();
    }

    @SuppressWarnings("unchecked")
    private List<String> getRepositories() {
        List<String> repos = new ArrayList<>();
        if (scmConfig != null && scmConfig.getConfig().get("repos") instanceof List) {
            repos.addAll((List<String>) scmConfig.getConfig().get("repos"));
        }
        return repos;
    }

    private String getCredentialsId(Item item) {
        String credentialId;
        if (scmConfig != null) {
            credentialId = scmConfig.getCredentialId();
        } else if (item instanceof OrganizationFolder) {
            GitHubSCMNavigator gitHubSCMNavigator = ((OrganizationFolder)item).getNavigators().get(GitHubSCMNavigator.class);
            credentialId = gitHubSCMNavigator.getScanCredentialsId();
        } else {
            credentialId = null;
        }
        return credentialId;
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
        public boolean isMatch(@Nonnull SCMNavigator navigator) {
            return this.navigator == navigator;
        }

        @Override
        public boolean isMatch(@Nonnull SCMSource source) {
            return ((GitHubSCMSource)source).getRepository().equals(getSourceName()) &&
                    source.getOwner().getFullName().equals(project.getFullName());
        }

        @Nonnull
        @Override
        public String getSourceName() {
            return repoName;
        }
    }

    private static class JenkinsfileCriteria implements SCMSourceCriteria{
        private static final long serialVersionUID = 1L;
        private AtomicBoolean jenkinsFileFound = new AtomicBoolean();

            @Override
            public boolean isHead(@Nonnull Probe probe, @Nonnull TaskListener listener) throws IOException {
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
