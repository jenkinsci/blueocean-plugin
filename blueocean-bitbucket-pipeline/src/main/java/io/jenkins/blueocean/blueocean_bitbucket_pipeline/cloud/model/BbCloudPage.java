package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BbCloudPage<T> extends BbPage<T> {
    private final int pageLength;
    private final int page;
    private final int size;
    private final String next;
    private final List<T> values;

    public BbCloudPage(@JsonProperty("pagelen") int pageLength,
                       @JsonProperty("page") int page,
                       @JsonProperty("size") int size,
                       @Nullable @JsonProperty("next") String next,
                       @Nonnull @JsonProperty("values") List<T> values) {
        this.pageLength = pageLength;
        this.page = page;
        this.size = size;
        this.next = next;
        this.values = values;
    }

    @Override
    public int getStart() {
        int start = pageLength*(page-1);
        if(start < 0){
            return 0;
        }
        return start;
    }

    @Override
    public int getLimit() {
        return pageLength;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public List<T> getValues() {
        return values;
    }

    @Override
    public boolean isLastPage() {
        return next == null;
    }
}
