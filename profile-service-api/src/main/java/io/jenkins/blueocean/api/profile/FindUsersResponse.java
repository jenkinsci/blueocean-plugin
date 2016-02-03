package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class FindUsersResponse {

    /** List of users */
    @JsonProperty("users")
    public final List<User> users;

    /**
     * Previous index of pipeline run from total
     */
    @JsonProperty("previous")
    public final Long previous;

    /**
     * Next index of pipeline run from total
     */
    @JsonProperty("next")
    public final Long next;


    public FindUsersResponse(@Nonnull @JsonProperty("users") List<User> users,
                             @Nullable @JsonProperty("previous") Long previous,
                             @Nullable @JsonProperty("next") Long next) {
        this.previous = previous;
        this.next = next;
        this.users = users;
    }
}
