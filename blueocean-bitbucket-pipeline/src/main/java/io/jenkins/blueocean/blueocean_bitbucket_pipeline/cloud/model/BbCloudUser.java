package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;

import javax.annotation.Nonnull;
import java.util.Map;

import static io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudTeam.avatar50;

/**
 * @author Vivek Pandey
 */
public class BbCloudUser extends BbUser {
    private final String userName;
    private final String displayName;
    private final String avatar;

    @JsonCreator
    public BbCloudUser(@Nonnull @JsonProperty("username") String userName,
                       @Nonnull @JsonProperty("display_name") String displayName,
                       @Nonnull @JsonProperty("links") Map<String, Map<String, String>> links) {
        this.userName = userName;
        this.displayName = displayName;
        Map<String,String> a = links.get("avatar");
        if(a != null){
            this.avatar = avatar50(a.get("href"));
        }else {
            this.avatar = null;
        }
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSlug() {
        return userName;
    }

    @Override
    public String getEmailAddress() {
        return null;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
