package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_MULTI_BRANCH_PIPELINE;

/**
 * Multi-branch pipeline model
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_MULTI_BRANCH_PIPELINE)
public abstract class BlueMultiBranchPipeline extends BluePipelineFolder implements BlueMultiBranchItem {
    public static final String TOTAL_NUMBER_OF_BRANCHES="totalNumberOfBranches";
    public static final String NUMBER_OF_FAILING_BRANCHES="numberOfFailingBranches";
    public static final String NUMBER_OF_SUCCESSFUL_BRANCHES="numberOfSuccessfulBranches";
    public static final String TOTAL_NUMBER_OF_PULL_REQUESTS="totalNumberOfPullRequests";
    public static final String NUMBER_OF_FAILING_PULL_REQUESTS="numberOfFailingPullRequests";
    public static final String NUMBER_OF_SUCCESSFUL_PULL_REQUESTS="numberOfSuccessfulPullRequests";
    public static final String BRANCH_NAMES ="branchNames";
    public static final String SCM_SOURCE = "scmSource";

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
    @Exported(name = NUMBER_OF_SUCCESSFUL_BRANCHES)
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
    @Exported(name = NUMBER_OF_SUCCESSFUL_PULL_REQUESTS)
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
     * MultiBranch pipeline is computed folder, no sub-folders in it
     */
    @Override
    public Iterable<String> getPipelineFolderNames() {
        return Collections.emptyList();
    }

    /**
     * @return It gives no-op {@link BlueRunContainer} since Multi-branch is not a build item, does not build on its own
     *
     */
    public BlueRunContainer getRuns(){
        return new BlueRunContainer() {
            @Override
            public Link getLink() {
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

            @Override
            public BlueRun create(StaplerRequest request) {
                throw new ServiceException.NotImplementedException("This action is not supported");
            }
        };
    }

    /**
     * Get metadata about the SCM for this pipeline.
     */
    @Exported(name = SCM_SOURCE, inline = true)
    public abstract BlueScmSource getScmSource();
}
