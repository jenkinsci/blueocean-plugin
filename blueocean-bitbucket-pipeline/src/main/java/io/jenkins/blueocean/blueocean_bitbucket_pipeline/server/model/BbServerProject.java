package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BbServerProject extends BbOrg {
    private final String key;
    private final String name;
    private final boolean publicProject;
    private final String selfLink;

    @JsonCreator
    public BbServerProject(@JsonProperty("key") String key, @JsonProperty("name") String name, @JsonProperty("public") boolean publicProject, @JsonProperty("links") Map<String, List<Map<String,String>>> links) {
        this.key = key;
        this.name = name;
        this.publicProject = publicProject;
        List<Map<String,String>> hrefs = links.get("self");
        String link=null;
        for(Map<String,String> hrefLink: hrefs){
            String href = hrefLink.get("href");
            if(href != null){
                link = href;
                break;
            }
        }
        this.selfLink = link;
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
        try {
            return new URIBuilder(selfLink+"/avatar.png").addParameter("s", "50").build().toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    @JsonProperty("public")
    public boolean isPublicProject() {
        return publicProject;
    }
}
