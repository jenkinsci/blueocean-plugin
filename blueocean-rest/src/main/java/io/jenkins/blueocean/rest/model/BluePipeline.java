package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.PUT;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
public abstract class BluePipeline extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String DISPLAY_NAME="displayName";
    public static final String FULL_NAME="fullName";
    public static final String WEATHER_SCORE ="weatherScore";
    public static final String LATEST_RUN = "latestRun";
    public static final String ESTIMATED_DURATION = "estimatedDurationInMillis";
    public static final String LAST_SUCCESSFUL_RUN = "lastSuccessfulRun";


    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    public abstract String getOrganization();

    /**
     * @return name of the pipeline
     */
    @Exported(name = NAME)
    public abstract String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    /**
     * @return Includes parent folders if any. For example folder1/folder2/p1
     */
    @Exported(name = FULL_NAME)
    public abstract String getFullName();

    /**
     * @return weather health score percentile
     */
    @Exported(name = WEATHER_SCORE)
    public abstract Integer getWeatherScore();

    /**
     * @return The Latest Run for the branch
     */
    @Exported(name = LATEST_RUN, inline = true)
    public abstract BlueRun getLatestRun();

    @Exported(name= LAST_SUCCESSFUL_RUN)
    public abstract String getLastSuccessfulRun();


    /**
     * @return Estiamated duration based on last pipeline runs. -1 is returned if there is no estimate available.
     *
     */
    @Exported(name = ESTIMATED_DURATION)
    public abstract Long getEstimatedDurationInMillis();

    /**
     * @return Gives Runs in this pipeline
     */
    @Navigable
    public abstract BlueRunContainer getRuns();

    @PUT
    @WebMethod(name="favorite")
    public abstract void favorite(@JsonBody FavoriteAction favoriteAction);


    /**
     *
     * BluePipeline resource link by default is in context of organization:
     *
     * e.g. /rest/organizations/jenkins/pipelines/pipeline1/
     *
     * In case of folders with nested pipelines or folders it follows recursive pattern
     *
     * /rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test1
     *
     * @return {@link Link} to self
     */
    @Override
    public Link getLink() {
        ApiHead apiHead = (ApiHead)Stapler.getCurrentRequest().findAncestor(ApiHead.class).getObject();

//        Ancestor pipelineContainer = Stapler.getCurrentRequest().findAncestor(BluePipelineContainer.class);
//
//        if(pipelineContainer != null){
            StringBuilder pipelinePath = new StringBuilder();
            String[] names = getFullName().split("/");
            int count = 1;
            if(names.length > 1) { //nested
                for (String n : names) {
                    if(count == 1){
                        pipelinePath.append(n);
                    }else{
                        pipelinePath.append("/pipelines/").append(n);
                    }
                    count++;
                }
            }else{
                pipelinePath.append(getFullName());
            }
            String href = String.format("organizations/%s/pipelines/%s/", getOrganization(), pipelinePath.toString());

            return new Link(apiHead.getLink().getHref() + href);
//        }
//
//        return super.getLink();
    }

    @Override
    public String getUrlName() {
        return getName();
    }

    public static class FavoriteAction {
        private boolean favorite;

        public void setFavorite(boolean favorite) {
            this.favorite = favorite;
        }

        public boolean isFavorite() {
            return favorite;
        }
    }
}
