package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import jenkins.branch.MultiBranchProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    public MultiBranchPipelineImpl(MultiBranchProject mbp) {
        this.mbp = mbp;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
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
    public int getTotalNumberOfBranches(){
        return mbp.getAllJobs().size();
    }

    @Override
    public int getNumberOfFailingBranches(){
        return countRunStatus(Result.FAILURE);
    }

    @Override
    public int getNumberOfSuccessfulBranches(){
        return countRunStatus(Result.SUCCESS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getMasterBranchStatusPercentile(){
        Job j = mbp.getBranch("master");
        if(j == null) {
            j = mbp.getBranch("production");
            if(j == null){ //get latest
                Collection<Job>  jbs = mbp.getAllJobs();
                if(jbs.size() > 0){
                    Job[] jobs = new Job[jbs.size()];
                    Arrays.sort(jobs, new Comparator<Job>() {
                        @Override
                        public int compare(Job o1, Job o2) {
                            long t1 = o1.getLastBuild().getTimeInMillis() + o1.getLastBuild().getDuration();
                            long t2 = o2.getLastBuild().getTimeInMillis() + o2.getLastBuild().getDuration();
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
    public BlueBranchContainer getBranches() {
        return new BranchContainerImpl(this);
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

    private int countRunStatus(Result result){
        Collection<Job> jobs = mbp.getAllJobs();
        int count=0;
        for(Job j:jobs){
            j.getBuildStatusUrl();
            Run run = j.getLastBuild();
            if(run.getResult() == result){
                count++;
            }
        }
        return count;
    }

}
