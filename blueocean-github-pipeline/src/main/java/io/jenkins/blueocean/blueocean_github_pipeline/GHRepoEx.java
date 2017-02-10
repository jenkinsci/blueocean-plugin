package io.jenkins.blueocean.blueocean_github_pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.github.GHRepository;

/**
 * Expose github repository 'private' field
 *
 * GHRepositry defines _private but doesn't get deserialized reliably with Jackson
 *
 * @author Vivek Pandey
 */
public class GHRepoEx extends GHRepository {
    @JsonProperty("private")
    private boolean _private;

    @Override
    public boolean isPrivate() {
        return _private;
    }
}
