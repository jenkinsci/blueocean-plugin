package io.blueocean.ath.model;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.BranchPageFactory;
import io.blueocean.ath.factory.RunDetailsArtifactsPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.factory.RunDetailsTestsPageFactory;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import io.blueocean.ath.pages.blue.RunDetailsTestsPage;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractPipeline {
    private Folder folder;
    private String name;

    @BaseUrl @Inject
    String baseUrl;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    BranchPageFactory branchPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPipelinePageFactory;

    @Inject
    RunDetailsArtifactsPageFactory runDetailsArtifactsPageFactory;

    @Inject
    RunDetailsTestsPageFactory runDetailsTestsPageFactory;

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

    public String getFullName() {
        if (folder.getPath() != null) {
            return folder.getPath().concat("/").concat(name);
        }

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
        return String.join("",
            baseUrl,
            "/blue/organizations/jenkins/",
            UrlEscapers.urlFragmentEscaper().escape(getUrlPart()).replaceAll("/", "%2F")
        );
    }

    public ActivityPage getActivityPage() {
        return activityPageFactory.withPipeline(this);
    }

    public BranchPage getBranchPage() {
        return branchPageFactory.withPipeline(this);
    }

    public RunDetailsPipelinePage getRunDetailsPipelinePage() {
        return runDetailsPipelinePageFactory.withPipeline(this);
    }

    public RunDetailsArtifactsPage getRunDetailsArtifactsPage() {
        return runDetailsArtifactsPageFactory.withPipeline(this);
    }

    public RunDetailsTestsPage getRunDetailsTestsPage() {
        return runDetailsTestsPageFactory.withPipeline(this);
    }

    public Predicate<List<JSONObject>> buildsFinished = list -> SSEEvents.activityComplete(getFolder().getPath(getName())).test(list);

}
