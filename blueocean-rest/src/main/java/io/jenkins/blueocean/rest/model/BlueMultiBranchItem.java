package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.Collection;

public interface BlueMultiBranchItem {

    // TODO: Replace all the ints with Integers etc

    /**
     * @return total number of branches
     */
    @Exported(name = "totalNumberOfBranches")
    int getTotalNumberOfBranches();

    /**
     * @return total number of failing branches
     */
    @Exported(name = "numberOfFailingBranches")
    int getNumberOfFailingBranches();

    /**
     * @return total number of successful branches
     */
    @Exported(name = "numberOfSuccessfulBranches")
    int getNumberOfSuccessfulBranches();

    /**
     * @return total number of pull requests
     */
    @Exported(name = "totalNumberOfPullRequests")
    int getTotalNumberOfPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = "numberOfFailingPullRequests")
    int getNumberOfFailingPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = "numberOfSuccessfulPullRequests")
    int getNumberOfSuccessfulPullRequests();

    /**
     * @return Gives {@link BluePipelineContainer}
     */
    BluePipelineContainer getBranches();

    /**
     * @return Gives array of branch names
     */
    @Exported(name = "branchNames")
    Collection<String> getBranchNames();
}
