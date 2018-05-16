package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.Collection;

public interface BlueMultiBranchItem {
    /**
     * @return total number of branches
     */
    @Exported(name = BlueMultiBranchPipeline.TOTAL_NUMBER_OF_BRANCHES)
    int getTotalNumberOfBranches();

    /**
     * @return total number of failing branches
     */
    @Exported(name = BlueMultiBranchPipeline.NUMBER_OF_FAILING_BRANCHES)
    int getNumberOfFailingBranches();

    /**
     * @return total number of successful branches
     */
    @Exported(name = BlueMultiBranchPipeline.NUMBER_OF_SUCCESSFULT_BRANCHES)
    int getNumberOfSuccessfulBranches();

    /**
     * @return total number of pull requests
     */
    @Exported(name = BlueMultiBranchPipeline.TOTAL_NUMBER_OF_PULL_REQUESTS)
    int getTotalNumberOfPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = BlueMultiBranchPipeline.NUMBER_OF_FAILING_PULL_REQUESTS)
    int getNumberOfFailingPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = BlueMultiBranchPipeline.NUMBER_OF_SUCCESSFULT_PULL_REQUESTS)
    int getNumberOfSuccessfulPullRequests();

    /**
     * @return Gives {@link BluePipelineContainer}
     */
    BluePipelineContainer getBranches();

    /**
     * @return Gives array of branch names
     */
    @Exported(name = BlueMultiBranchPipeline.BRANCH_NAMES)
    Collection<String> getBranchNames();
}
