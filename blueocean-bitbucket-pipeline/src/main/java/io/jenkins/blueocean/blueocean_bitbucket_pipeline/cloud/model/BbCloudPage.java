package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
                       @NonNull @JsonProperty("values") List<T> values) {
        this.pageLength = pageLength;
        this.page = page;
        this.size = size;
        this.next = next;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    @Override
    public int getStart() {
        int start = pageLength*(page-1);
        return Math.max( start, 0 );
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

    public String getNext(){
        return next;
    }

    @Override
    public boolean isLastPage() {
        return next == null;
    }
}
