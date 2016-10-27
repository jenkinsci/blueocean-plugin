package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineCreatorFactory;
import io.jenkins.blueocean.rest.model.BluePipelineFolder;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.BlueFavoriteResolver;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.FavoriteImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.kohsuke.stapler.json.JsonBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_MULTI_BRANCH_PROJECT;

/**
 * @author Vivek Pandey
 */
@Capability({JENKINS_MULTI_BRANCH_PROJECT})
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    private final Link self;
    public MultiBranchPipelineImpl(MultiBranchProject mbp) {
        this.mbp = mbp;
        this.self = OrganizationImpl.INSTANCE.getLink().rel("pipelines").rel(PipelineImpl.getRecursivePathFromFullName(this));
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }


    @Override
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }

        Job job = mbp.getItem("master");
        if(job == null) {
            throw new ServiceException.BadRequestExpception("no master branch to favorite");
        }

        FavoriteUtil.favoriteJob(mbp.getFullName(), favoriteAction.isFavorite());

        return new FavoriteImpl(new BranchImpl(job,getLink().rel("branches")), getLink().rel("favorite"));
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return AbstractPipelineImpl.getPermissions(mbp);
    }

    @Override
    public String getName() {
        return mbp.getName();
    }

    @Override
    public String getDisplayName() {
        return mbp.getDisplayName();
    }

    @Override
    public String getFullName() {
        return mbp.getFullName();
    }

    @Override
    public String getFullDisplayName() {
        return AbstractPipelineImpl.getFullDisplayName(mbp, null);
    }

    @Override
    public int getTotalNumberOfBranches(){
        return countJobs(false);
    }

    @Override
    public int getNumberOfFailingBranches(){
        return countRunStatus(Result.FAILURE, false);
    }

    @Override
    public int getNumberOfSuccessfulBranches(){
        return countRunStatus(Result.SUCCESS, false);
    }

    @Override
    public int getTotalNumberOfPullRequests() {
        return countJobs(true);
    }

    @Override
    public int getNumberOfFailingPullRequests() {
        return countRunStatus(Result.FAILURE, true);
    }

    @Override
    public int getNumberOfSuccessfulPullRequests() {
        return countRunStatus(Result.SUCCESS, true);
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return new BranchContainerImpl(this, getLink().rel("pipelines"));
    }

    @Override
    public Integer getNumberOfFolders() {
        return 0;
    }

    @Override
    public Integer getNumberOfPipelines() {
        return getTotalNumberOfBranches();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer getWeatherScore(){
        /**
         * TODO: this code need cleanup once MultiBranchProject exposes default branch. At present
         *
         * At present we look for master as primary branch, if not found we find the latest build across all branches and
         * return its score.
         *
         * If there are no builds taken place 0 score is returned.
         */

        Job j = mbp.getItem("master");
        if(j == null) {
            j = mbp.getItem("production");
            /**
             * If there are no master or production branch then we return weather score of
             *
             * - Sort latest build of all branches in ascending order
             * - Return the latest
             *
             */
            if(j == null){
                Collection<Job>  jbs = mbp.getAllJobs();
                if(jbs.size() > 0){
                    Job[] jobs = jbs.toArray(new Job[jbs.size()]);
                    Arrays.sort(jobs, new Comparator<Job>() {
                        @Override
                        public int compare(Job o1, Job o2) {
                            long t1 = 0;
                            if(o1.getLastBuild() != null){
                                t1 = o1.getLastBuild().getTimeInMillis() + o1.getLastBuild().getDuration();
                            }

                            long t2 = 0;
                            if(o2.getLastBuild() != null){
                                t2 = o2.getLastBuild().getTimeInMillis() + o2.getLastBuild().getDuration();
                            }

                            if(t1<2){
                                return -1;
                            }else if(t1 > t2){
                                return 1;
                            }else{
                                return 0;
                            }
                        }
                    });

                    return jobs[jobs.length - 1].getBuildHealth().getScore();
                }
            }
        }
        return j == null ? 0 : j.getBuildHealth().getScore();
    }

    @Override
    public BlueRun getLatestRun() {
        //For multibranch is a folder that is no run of itself.
        return null;
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        return mbp.getEstimatedDuration();
    }

    @Override
    public String getLastSuccessfulRun() {
        return null;
    }

    @Override
    @Navigable
    public BluePipelineContainer getBranches() {
        return new BranchContainerImpl(this, getLink().rel("branches"));
    }

    @Override
    public Collection<String> getBranchNames() {
        Collection<Job> jobs =  mbp.getAllJobs();
        List<String> branches = new ArrayList<>();
        for(Job j : jobs){
            branches.add(j.getName());
        }
        return branches;
    }

    private int countRunStatus(Result result, boolean pullRequests){
        Collection<Job> jobs = mbp.getAllJobs();
        int count=0;
        for(Job j:jobs){
            if(pullRequests && isPullRequest(j) || !pullRequests && !isPullRequest(j)) {
                j.getBuildStatusUrl();
                Run run = j.getLastBuild();
                if (run!=null && run.getResult() == result) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countJobs(boolean pullRequests) {
        Collection<Job> jobs = mbp.getAllJobs();
        int counter = 0;

        for(Job job: jobs){
            if(pullRequests && isPullRequest(job) || !pullRequests && !isPullRequest(job)) {
                counter += 1;
            }
        }

        return counter;
    }
    private boolean isPullRequest(Job job) {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        return head != null && head.getAction(ChangeRequestAction.class) != null;
    }


    @Override
    public BlueRunContainer getRuns() {
        return new BlueRunContainer() {
            @Override
            public Link getLink() {
                return MultiBranchPipelineImpl.this.getLink().rel("runs");
            }

            @Override
            public BluePipeline getPipeline(String name) {
                return null;
            }

            @Override
            public BlueRun get(String name) {
                return null;
            }

            @Override
            public Iterator<BlueRun> iterator() {
                List<BlueRun> c = new ArrayList<>();
                for(final BluePipeline b: getBranches()) {
                    for(final BlueRun r: b.getRuns()) {
                        c.add(r);
                    }
                }
                Collections.sort(c, new Comparator<BlueRun>() {
                    @Override
                    public int compare(BlueRun o1, BlueRun o2) {
                        return o2.getStartTime().compareTo(o1.getStartTime());
                    }
                });

                return c.iterator();
            }

            @Override
            public BlueQueueItem create() {
                throw new ServiceException.NotImplementedException("This action is not supported");
            }
        };
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return PipelineImpl.getActionProxies(mbp.getAllActions(), this);
    }

    @Override
    public BlueQueueContainer getQueue() {
        return new MultiBranchPipelineQueueContainer(this);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Extension(ordinal = 2)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public MultiBranchPipelineImpl getPipeline(Item item, Reachable parent) {
            if (item instanceof MultiBranchProject) {
                return new MultiBranchPipelineImpl((MultiBranchProject) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            if (context instanceof MultiBranchProject) {
                if (context==target)
                    return getPipeline(context,parent);
                if (context==target.getParent()) {
                    // target is a branch
                    return getPipeline(context,parent).getBranches().get(target.getName());
                }
            }
            return null;
        }

    }

    @Extension
    public static class PipelineCreatorFactoryImpl extends BluePipelineCreatorFactory{
        @Override
        public BluePipeline create(Map<String, Object> request, Reachable parent) throws IOException {
            ScmOrganizationPipelineRequest scmOrganizationPipelineRequest = ScmOrganizationPipelineRequest.getInstance(request);
            if(scmOrganizationPipelineRequest == null){
                return null;
            }
            ScmProvider scmProvider=null;
            for(ScmProviderFactory f:ScmProviderFactory.all()){
                scmProvider = f.getScmProvider(scmOrganizationPipelineRequest.scmProviderId);
                if(scmProvider == null){
                    continue;
                }
                break;
            }
            if(scmProvider == null){
                throw new ServiceException.BadRequestExpception("Unkown scmProviderId: "+ scmOrganizationPipelineRequest.scmProviderId);
            }

//            CredentialsMatcher matcher = CredentialsMatchers.withId(scmOrganizationPipelineRequest.credentialId);
//            List<StandardUsernamePasswordCredentials> credentials=CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class);
//            StandardUsernamePasswordCredentials cred = CredentialsMatchers.firstOrNull(credentials, matcher);
//            if(cred == null){
//                throw new ServiceException.BadRequestExpception("Credential with id: "+ scmOrganizationPipelineRequest.credentialId+" not found");
//            }

            return scmProvider.createProjects(scmOrganizationPipelineRequest, Jenkins.getInstance(), parent.getLink());
        }
    }

    @Extension
    public static class GithubScmProviderFactory extends ScmProviderFactory implements ScmProvider{
        private static final String NAVIGATOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";
        private CustomOrganizationFolderDescriptor descriptor;
        @Override
        public ScmProvider getScmProvider(String id) {
            if(id != null && id.equals("github")){
                TopLevelItemDescriptor descriptor = Items.all().findByName(NAVIGATOR);
                if(descriptor == null || !(descriptor instanceof CustomOrganizationFolderDescriptor)){
                    return null;
                }
                descriptor.checkApplicableIn(Jenkins.getInstance());
                this.descriptor = (CustomOrganizationFolderDescriptor) descriptor;
                return this;
            }
            return null;
        }

        @Override
        public BluePipelineFolder createProjects(ScmOrganizationPipelineRequest request, ItemGroup parent, Link parentLink) throws IOException{
            ACL acl = Jenkins.getInstance().getACL();
            acl.checkPermission(Item.CREATE);
            descriptor.checkApplicableIn(parent);
            acl.checkCreatePermission(parent, descriptor);
            Item item = Jenkins.getInstance().createProject(descriptor, request.orgName, true);

            GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(request.apiUrl, request.getOrgName(), request.credentialId, request.credentialId);
            StringBuilder sb = new StringBuilder();
            for(String r:request.repos){
                sb.append(String.format("(%s\\b)?", r));
            }

            if(sb.length() > 0){
                gitHubSCMNavigator.setPattern(sb.toString());
            }

            if(item instanceof OrganizationFolder){
                OrganizationFolder organizationFolder = (OrganizationFolder) item;
                organizationFolder.getNavigators().replace(gitHubSCMNavigator);
                organizationFolder.scheduleBuild();
                return new PipelineFolderImpl(organizationFolder, parentLink);
            }
            return null;
        }
    }

    public static class ScmOrganizationPipelineRequest {
        private final String scmProviderId;
        private final String orgName;
        private final String apiUrl;
        private final String credentialId;
        private final List<String> repos = new ArrayList<>();

        public ScmOrganizationPipelineRequest(String apiUrl, String orgName, String scmProviderId, String credentialId) {
            this.orgName = orgName;
            this.scmProviderId = scmProviderId;
            this.credentialId = credentialId;
            this.apiUrl = apiUrl;
        }

        public String getOrgName() {
            return orgName;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        @SuppressWarnings("unchecked")
        public static ScmOrganizationPipelineRequest getInstance(Map<String, Object> request){
            String scmProviderId=null;
            String credentialId=null;
            String orgName = null;
            String apiUrl = null;

            if(request.get("name") instanceof String){
                orgName = (String) request.get("name");
            }

            if(request.get("apiUrl") instanceof String){
                apiUrl = (String) request.get("apiUrl");
            }


            if(request.get("scmProviderId") instanceof String){
                scmProviderId = (String) request.get("scmProviderId");
            }

            if(request.get("credentialId") instanceof String){
                credentialId = (String) request.get("credentialId");
            }

            if(scmProviderId != null && orgName != null) {
                ScmOrganizationPipelineRequest scmOrganizationPipelineRequest = new ScmOrganizationPipelineRequest(apiUrl, orgName, scmProviderId, credentialId);
                if(request.get("repos") instanceof List){
                    scmOrganizationPipelineRequest.repos.addAll((Collection<? extends String>) request.get("repos"));
                }
                return scmOrganizationPipelineRequest;
            }else{
                return null;
            }
        }

        public String getScmProviderId() {
            return scmProviderId;
        }


        public String getCredentialId() {
            return credentialId;
        }

        public List<String> getRepos() {
            return repos;
        }

        public void addRepos(List<String> repos){
            this.repos.addAll(repos);
        }
    }



    @Extension(ordinal = 1)
    public static class FavoriteResolverImpl extends BlueFavoriteResolver {
        @Override
        public BlueFavorite resolve(Item item, Reachable parent) {
            if(item instanceof MultiBranchProject){
                MultiBranchProject project = (MultiBranchProject) item;
                Job job = project.getItem("master");
                if(job != null){
                    Resource resource = BluePipelineFactory.resolve(job);
                    Link l = LinkResolver.resolveLink(project);
                    if(l != null) {
                        return new FavoriteImpl(resource, l.rel("favorite"));
                    }
                }
            }
            return null;
        }
    }

    @Navigable
    public Container<Resource> getActivities() {
        return Containers.fromResource(getLink(), Lists.newArrayList(Iterators.concat(getQueue().iterator(), getRuns().iterator())));
    }
}
