package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.List;

/**
 * BitBucket pagination.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbPage<T> {
    /**
     * @return Starting index, starting from 0
     */
    @JsonProperty("start")
    public abstract int getStart();

    /**
     * @return Requested page size
     */
    @JsonProperty("limit")
    public abstract int getLimit();

    /**
     * @return Number of items in this page
     */
    @JsonProperty("size")
    public abstract int getSize();

    /**
     * @return Items in this page
     */
    @JsonProperty("values")
    public abstract List<T> getValues();

    /**
     * @return true if this is last page
     */
    @JsonProperty("isLastPage")
    public abstract boolean isLastPage();
}
