package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Vivek Pandey
 */
@ExportedBean
public class ChangeSetWrapper{
    private final ChangeLogSet.Entry changeSet;
    public ChangeSetWrapper(ChangeLogSet.Entry changeSet) {
        this.changeSet = changeSet;
    }


    @Exported
    public String getMsg() {
        return changeSet.getMsg();
    }

    @Exported
    public User getAuthor() {
        return User.get(changeSet.getAuthor().getId(), false, Collections.emptyMap());
    }

    @Exported
    public Collection<String> getAffectedPaths() {
        return changeSet.getAffectedPaths();
    }
}
