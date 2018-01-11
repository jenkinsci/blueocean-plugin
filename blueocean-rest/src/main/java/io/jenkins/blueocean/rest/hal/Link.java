package io.jenkins.blueocean.rest.hal;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Objects;

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
        assert this.href.endsWith("/");
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


    /**
     * Gives ancestor Link, for example, for link("/a/b/c/d"), ancestor is "a/b/c/".
     */
    public Link ancestor(){
        int i = href.lastIndexOf("/");

        if(i>0) {
            int j = href.substring(0, i).lastIndexOf("/");

            if (j > 0) {
                return new Link(href.substring(0, j));
            }
        }

        return new Link("/");
    }

    @Override
    public String toString() {
        return href;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return Objects.equals(href, link.href);
    }

    @Override
    public int hashCode() {

        return Objects.hash(href);
    }
}
