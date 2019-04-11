package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.rest.Utils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BbServerUser extends BbUser {
    private final String displayName;
    private final String slug;
    private final String emailAddress;
    private final String avatar;

    @JsonCreator
    public BbServerUser(@Nonnull @JsonProperty("displayName") String displayName,
                        @Nonnull @JsonProperty("slug") String slug, @Nonnull @JsonProperty("emailAddress") String emailAddress, @JsonProperty("links") Map<String, List<Map<String,String>>> links) {
        this.displayName = displayName;
        this.slug = slug;
        this.emailAddress = emailAddress;
        List<Map<String,String>> hrefs = links.get("self");
        String a =null;
        for(Map<String,String> hrefLink: hrefs){
            String href = hrefLink.get("href");
            if(href != null){
                a = Utils.ensureTrailingSlash(href)+"avatar.png?s=50";
                break;
            }
        }
        this.avatar = a;
    }

    @Override
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    @Override
    @JsonProperty("emailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    /**
     * Every user account on bitbucket server is a project with key ~{userSlug}.
     * see 'Personal Repositories' section at https://developer.atlassian.com/static/rest/bitbucket-server/5.3.1/bitbucket-rest.html
     * @return project key for user account
     */
    @JsonIgnore
    public BbServerProject toPersonalProject(){
        return new BbServerProject("~"+slug, getDisplayName(), avatar);
    }
}
