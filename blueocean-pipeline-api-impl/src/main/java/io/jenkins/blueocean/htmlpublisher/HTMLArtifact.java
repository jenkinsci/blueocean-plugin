package io.jenkins.blueocean.htmlpublisher;

import com.google.common.collect.Lists;
import htmlpublisher.HtmlPublisherTarget;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueArtifactFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;

import java.util.Collection;
import java.util.List;

public class HTMLArtifact extends BlueArtifact {

    private final HtmlPublisherTarget.HTMLBuildAction action;

    public HTMLArtifact(HtmlPublisherTarget.HTMLBuildAction action, Link parent) {
        super(parent);
        this.action = action;
    }

    @Override
    public String getName() {
        return action.getHTMLTarget().getReportName();
    }

    @Override
    public String getPath() {
        return action.getHTMLTarget().getReportName();
    }

    @Override
    public String getUrl() {
        return String.format("/%s%s", action.getOwner().getUrl(), action.getUrlName());
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public boolean isDownloadable() {
        return false;
    }

    @Extension
    public static class FactoryImpl extends BlueArtifactFactory {
        @Override
        public Collection<BlueArtifact> getArtifacts(Run<?, ?> run, Reachable parent) {
            List<HtmlPublisherTarget.HTMLBuildAction> actions = run.getActions(HtmlPublisherTarget.HTMLBuildAction.class);
            if (actions.isEmpty()) {
                return null;
            }
            List<BlueArtifact> foundArtifacts = Lists.newArrayList();
            for (HtmlPublisherTarget.HTMLBuildAction action : actions) {
                foundArtifacts.add(new HTMLArtifact(action, parent.getLink()));
            }
            return foundArtifacts;
        }
    }
}
