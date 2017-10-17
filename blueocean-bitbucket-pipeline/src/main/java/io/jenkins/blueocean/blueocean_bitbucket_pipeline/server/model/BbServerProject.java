package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BbServerProject extends BbOrg {
    private static final Logger logger = LoggerFactory.getLogger(BbServerProject.class);

    private final String key;
    private final String name;
    private final boolean publicProject;
    private final String avatar;

    @JsonCreator
    public BbServerProject(@JsonProperty("key") String key, @JsonProperty("name") String name, @JsonProperty("public") boolean publicProject, @JsonProperty("links") Map<String, List<Map<String,String>>> links) {
        this.key = key;
        this.name = name;
        this.publicProject = publicProject;
        List<Map<String,String>> hrefs = links.get("self");
        String a=null;
        for(Map<String,String> hrefLink: hrefs){
            String href = hrefLink.get("href");
            if(href != null){
                try {
                    a = new URIBuilder(href + "/avatar.png").addParameter("s", "50").build().toString();
                } catch (URISyntaxException e) {
                    logger.warn("Failed to construct Bitbucket server avatar URL: "+e.getMessage(), e);
                }
                break;
            }
        }
        this.avatar = a;
    }

    public BbServerProject(String key, String name, String avatar) {
        this.key = key;
        this.name = name;
        this.publicProject = false;
        this.avatar = avatar;
    }

    @Override
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonProperty("avatar")
    public String getAvatar(){
        return avatar;
    }

    @Override
    @JsonProperty("public")
    public boolean isPublicProject() {
        return publicProject;
    }
}
