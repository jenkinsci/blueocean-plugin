package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;

import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.PUT;

/**
 * @author Vivek Pandey
 */
public class BranchImpl extends PipelineImpl {

    private static final String PULL_REQUEST = "pullRequest";

    public BranchImpl(Job job) {
        super(job);
    }

    @Exported(name = PULL_REQUEST, inline = true)
    public PullRequest getPullRequest() {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        if(head != null) {
            ChangeRequestAction action = head.getAction(ChangeRequestAction.class);
            if(action != null){
                return new PullRequest(action.getId(), action.getURL().toExternalForm(), action.getTitle(), action.getAuthor());
            }
        }
        return null;
    }


    public static class PullRequest extends Resource {
        private static final String PULL_REQUEST_NUMBER = "id";
        private static final String PULL_REQUEST_AUTHOR = "author";
        private static final String PULL_REQUEST_TITLE = "title";
        private static final String PULL_REQUEST_URL = "url";

        private final String id;

        private final String url;

        private final String title;

        private final String author;

        public PullRequest(String id, String url, String title, String author) {
            this.id = id;
            this.url = url;
            this.title = title;
            this.author = author;
        }

        @Exported(name = PULL_REQUEST_NUMBER)
        public String getId() {
            return id;
        }


        @Exported(name = PULL_REQUEST_URL)
        public String getUrl() {
            return url;
        }


        @Exported(name = PULL_REQUEST_TITLE)
        public String getTitle() {
            return title;
        }


        @Exported(name = PULL_REQUEST_AUTHOR)
        public String getAuthor() {
            return author;
        }
    }

}
