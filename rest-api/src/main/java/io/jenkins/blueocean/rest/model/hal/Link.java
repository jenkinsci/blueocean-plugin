package io.jenkins.blueocean.rest.model.hal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vivek Pandey
 **/
public class Link {
    public final String href;

    public Link(@JsonProperty("href") String href) {
        this.href = href;
    }
}
