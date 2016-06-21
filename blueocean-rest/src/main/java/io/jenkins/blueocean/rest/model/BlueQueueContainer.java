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
}
