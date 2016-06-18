package io.jenkins.blueocean.rest.hal;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Link holding reference to a resource.
 *
 * @author Vivek Pandey
 * @see Links
 **/
@ExportedBean(defaultVisibility = 99999)
public final class Link {
    private final String href;

    public Link(String href) {
        this.href = Links.ensureTrailingSlash(href);
    }

    @Exported(name = "href")
    public String getHref(){
        return href;
    }

    /**
     * Resolves a relative reference.
     */
    public Link rel(String name) {
        return new Link(href+name);
    }
}
