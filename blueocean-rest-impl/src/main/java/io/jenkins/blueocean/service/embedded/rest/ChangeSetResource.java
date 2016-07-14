package io.jenkins.blueocean.service.embedded.rest;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.RepositoryBrowser;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Represents a single commit as a REST resource.
 *
 * <p>
 * Mostly we just let {@link ChangeLogSet.Entry} serve its properties,
 * except a few places where we are more specific.
 *
 * @author Vivek Pandey
 */
@ExportedBean
public class ChangeSetResource extends Resource {
    private final ChangeLogSet.Entry changeSet;
    private final Reachable parent;

    public ChangeSetResource(Entry changeSet, Reachable parent) {
        this.changeSet = changeSet;
        this.parent = parent;
    }

    @Exported(merge=true)
    public ChangeLogSet.Entry getDelegate() {
        return changeSet;
    }

    @Exported(inline = true)
    public BlueUser getAuthor() {
        return new UserImpl(changeSet.getAuthor());
    }

    @Exported
    public String getTimestamp(){
        if(changeSet.getTimestamp() > 0) {
            return new SimpleDateFormat(BlueRun.DATE_FORMAT_STRING).format(changeSet.getTimestamp());
        }else{
            return null;
        }
    }

    @Exported
    public String getUrl() {
        RepositoryBrowser browser = changeSet.getParent().getBrowser();
        if(browser != null) {
            try {
                URL url =  browser.getChangeSetLink(changeSet);
                return url == null ? null : url.toExternalForm();
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("changeset/"+getDelegate().getCommitId());
    }
}
