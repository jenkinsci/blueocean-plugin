package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BbCloudTeam extends BbOrg {
    private final String teamName;
    private final String displayName;
    private final String avatar;

    @JsonCreator
    public BbCloudTeam(@Nonnull @JsonProperty("username") String teamName,
                       @Nonnull @JsonProperty("display_name") String displayName,
                       @Nonnull @JsonProperty("links") Map<String, Map<String, String>> links) {
        this.teamName = teamName;
        this.displayName = displayName;
        Map<String,String> a = links.get("avatar");
        if(a != null){
            this.avatar = avatar50(a.get("href"));
        }else {
            this.avatar = null;
        }
    }

    @Override
    public String getKey() {
        return teamName;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean isPublicProject() {
        return false;
    }

    //Get size 50 avatar
    static String avatar50(String href){
        if(StringUtils.isBlank(href)){
            return null;
        }
        int i = href.lastIndexOf("/avatar/");
        if(i != -1){
            return href.substring(0,i)+"/avatar/50/";
        }
        return href;
    }
}
