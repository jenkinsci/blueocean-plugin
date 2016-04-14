package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Multi-branch pipeline model
 *
 * @author Vivek Pandey
 */
public abstract class BlueMultiBranchPipeline extends BluePipeline{
    public static final String TOTAL_NUMBER_OF_BRANCHES="totalNumberOfBranches";
    public static final String NUMBER_OF_FAILING_BRANCHES="numberOfFailingBranches";
    public static final String NUMBER_OF_SUCCESSFULT_BRANCHES="numberOfSuccessfulBranches";
    public static final String TOTAL_NUMBER_OF_PULL_REQUESTS="totalNumberOfPullRequests";
    public static final String NUMBER_OF_FAILING_PULL_REQUESTS="numberOfFailingPullRequests";
    public static final String NUMBER_OF_SUCCESSFULT_PULL_REQUESTS="numberOfSuccessfulPullRequests";
    public static final String BRANCH_NAMES ="branchNames";

    /**
     * @return total number of branches
     */
    @Exported(name = TOTAL_NUMBER_OF_BRANCHES)
    public abstract int  getTotalNumberOfBranches();

    /**
     * @return total number of failing branches
     */
    @Exported(name = NUMBER_OF_FAILING_BRANCHES)
    public abstract int getNumberOfFailingBranches();

    /**
     * @return total number of successful branches
     */
    @Exported(name = NUMBER_OF_SUCCESSFULT_BRANCHES)
    public abstract int getNumberOfSuccessfulBranches();

    /**
    * @return total number of pull requests
    */
    @Exported(name = TOTAL_NUMBER_OF_PULL_REQUESTS)
    public abstract int  getTotalNumberOfPullRequests();
    /**
     * @return total number of pull requests
     */
    @Exported(name = NUMBER_OF_FAILING_PULL_REQUESTS)
    public abstract int getNumberOfFailingPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = NUMBER_OF_SUCCESSFULT_PULL_REQUESTS)
    public abstract int getNumberOfSuccessfulPullRequests();

    /**
     * @return Gives {@link BluePipelineContainer}
     */
    public abstract BluePipelineContainer getBranches();

    /**
     * @return Gives array of branch names
     */
    @Exported(name = BRANCH_NAMES)
    public abstract Collection<String> getBranchNames();

    /**
     * @return It gives no-op {@link BlueRunContainer} since Multi-branch is not a build item, does not build on its own
     *
     */
    public BlueRunContainer getRuns(){
        return new BlueRunContainer() {
            @Override
            public BluePipeline getPipeline(String name) {
                return null;
            }

            @Override
            public BlueRun get(String name) {
                throw new ServiceException.NotFoundException(
                    String.format("It is multi-branch project. No run with name: %s found.", name));
            }

            @Override
            public Iterator<BlueRun> iterator() {
                return Collections.emptyIterator();
            }
        };
    }
}
