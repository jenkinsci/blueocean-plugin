package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.JsonOutputFilter;
import org.kohsuke.stapler.json.JsonResponse;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.verb.PUT;

/**
 * @author Ivan Meredith
 */
public abstract class BlueQueueContainer extends Container<BlueQueueItem> {
    @WebMethod(name = "new")
    @JsonResponse
    @JsonOutputFilter(excludes = "state")
    @POST
    public abstract BlueQueueItemCreateResponse createItem();

    /**
     * Response object for create item. is a mirror of BlueQueueItem.
     */
    public static class BlueQueueItemCreateResponse {
        private BlueQueueItem item;

        public BlueQueueItemCreateResponse(BlueQueueItem item) {
            this.item = item;

        }

        public String getId() {
            return item.getId();
        }

        public String getPipeline() {
            return item.getPipeline();
        }

        public String getQueuedTime() {
            return item.getQueuedTimeString();
        }

        public int getExpectedBuildNumber() {
            return item.getExpectedBuildNumber();
        }
    }
}
