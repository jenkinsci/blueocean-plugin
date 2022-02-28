package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.RepositoryBrowser;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueIssue;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueUser;
import org.kohsuke.stapler.export.ExportedBean;


import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;

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
public class ChangeSetResource extends BlueChangeSetEntry {
    private final ChangeLogSet.Entry changeSet;
    private final Reachable parent;
    private final BlueOrganization organization;
    private int checkoutCount;

    public ChangeSetResource(@NonNull BlueOrganization organization, Entry changeSet, Reachable parent) {
        this.organization = organization;
        this.changeSet = changeSet;
        this.parent = parent;
    }


    @Override
    public BlueUser getAuthor() {
        return new UserImpl(organization, changeSet.getAuthor());
    }

    @Override
    public String getTimestamp(){
        if(changeSet.getTimestamp() > 0) {
            return AbstractRunImpl.DATE_FORMAT.format(Instant.ofEpochMilli(changeSet.getTimestamp()));
        }else{
            return null;
        }
    }

    @Override
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
    public String getCommitId() {
        return changeSet.getCommitId();
    }

    @Override
    public String getMsg() {
        return changeSet.getMsg();
    }

    @Override
    public Collection<String> getAffectedPaths() {
        return changeSet.getAffectedPaths();
    }

    @Nullable
    @Override
    public Collection<BlueIssue> getIssues() {
        return BlueIssueFactory.resolve(changeSet);
    }

    @Override
    public Integer getCheckoutCount() {
        return this.checkoutCount;
    }

    @Override
    public BlueChangeSetEntry setCheckoutCount(int checkoutCount) {
        this.checkoutCount = checkoutCount;
        return this;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel(getCommitId());
    }
}
