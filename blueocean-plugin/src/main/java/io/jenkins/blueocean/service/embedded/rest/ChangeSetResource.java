package io.jenkins.blueocean.service.embedded.rest;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

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

    public ChangeSetResource(Entry changeSet) {
        this.changeSet = changeSet;
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
}
