package io.jenkins.blueocean.blueocean_github_pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.GHRepository;

/**
 * Expose github repository 'private' field
 *
 * GHRepositry defines _private but doesn't get deserialized reliably with Jackson
 *
 * @author Vivek Pandey
 */
@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "no extra state added to affect superclass equality")
public class GHRepoEx extends GHRepository {
    @JsonProperty("private")
    private boolean _private;

    @Override
    public boolean isPrivate() {
        return _private;
    }
}
