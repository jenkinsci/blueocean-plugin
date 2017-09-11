package io.blueocean.ath.model;

import com.google.common.base.Joiner;
import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.RunDetailsArtifactsPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;

public abstract class AbstractPipeline {
    private Folder folder;
    private String name;

    @BaseUrl @Inject
    String baseUrl;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPipelinePageFactory;

    @Inject
    RunDetailsArtifactsPageFactory runDetailsArtifactsPageFactory;

    public AbstractPipeline(String name) {
        this(null, name);
    }

    public AbstractPipeline(Folder folder, String name) {
        if(folder == null) {
            this.folder = Folder.folders();
        } else {
            this.folder = folder;
        }
        this.name = name;
    }

    public boolean isMultiBranch() {
        return false;
    }

    public Folder getFolder() {
        return folder;
    }

    public String getName() {
        return name;
    }

    public String getUrlPart() {
        String part = "";

        if(folder != null) {
            part += folder.getPath();
        }
        if(part.length() != 0) {
            part += "/";
        }
        return part + name;
    }

    public String getUrl() {
        return Joiner.on("").join(
            baseUrl,
            "/blue/organizations/jenkins/",
            UrlEscapers.urlFragmentEscaper().escape(getUrlPart()).replaceAll("/", "%2F")
        );
    }

    public ActivityPage getActivityPage() {
        return activityPageFactory.withPipeline(this);
    }

    public RunDetailsPipelinePage getRunDetailsPipelinePage() {
        return runDetailsPipelinePageFactory.withPipeline(this);
    }

    public RunDetailsArtifactsPage getRunDetailsArtifactsPage() {
        return runDetailsArtifactsPageFactory.withPipeline(this);
    }
}
