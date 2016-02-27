package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.branch.MultiBranchProject;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchBluePipeline extends BluePipeline {
    /*package*/ final MultiBranchProject mbp;

    public MultiBranchBluePipeline(MultiBranchProject mbp) {
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

    @JsonProperty("totalNumberOfBranches")
    @Exported
    public int getTotalNumberOfBranches(){
        return mbp.getAllJobs().size();
    }

    @JsonProperty("totalNumberOfFailingBranches")
    @Exported
    public int getNumberOfFailingBranches(){
        return countRunStatus(Result.FAILURE);
    }

    @JsonProperty("totalNumberOfSuccessfulBranches")
    @Exported
    public int getNumberOfSuccessfulBranches(){
        return countRunStatus(Result.SUCCESS);
    }

    @Override
    public BlueBranchContainer getBranches() {
        return new BranchContainerImpl(this);
    }

    @Exported(name = "branches")
    public Collection<String> getBranchNames() {
        Collection<Job> jobs =  mbp.getAllJobs();
        List<String> branches = new ArrayList<>();
        for(Job j : jobs){
            branches.add(j.getName());
        }
        return branches;
    }


    // This is MultiBranchProject, it doesn't have build of it's own. Its a Folder.
    @Override
    public BlueRunContainer getRuns() {
        return new BlueRunContainer() {
            @Override
            public BluePipeline getPipeline(String name) {
                return null;
            }

            @Override
            public BlueRun get(String name) {
                throw new ServiceException.NotFoundException(
                    String.format("%s is multi-branch project. No run with name: %s found.", mbp.getName(),name));
            }

            @Override
            public Iterator<BlueRun> iterator() {
                return Collections.emptyIterator();
            }
        };
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
