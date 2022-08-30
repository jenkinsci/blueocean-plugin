package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

import static io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudTeam.avatar50;

/**
 * @author Vivek Pandey
 */
public class BbCloudUser extends BbUser {
    private final String uuid;
    private final String displayName;
    private final String nickName;
    private final String accountId;
    private final String avatar;

    @JsonCreator
    public BbCloudUser(@NonNull @JsonProperty("uuid") String uuid,
                       @NonNull @JsonProperty("display_name") String displayName,
                       @NonNull @JsonProperty("links") Map<String, Map<String, String>> links,
                       @NonNull @JsonProperty("nickname") String nickName,
                       @NonNull @JsonProperty("account_id") String accountId
    ) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.nickName = nickName;
        this.accountId = accountId;
        Map<String,String> a = links.get("avatar");
        if(a != null){
            this.avatar = avatar50(a.get("href"));
        }else {
            this.avatar = null;
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSlug() {
        return uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNickName() {
        return nickName;
    }

    public String getAccountId() {
        return accountId;
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
