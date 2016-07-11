package io.jenkins.blueocean.rest.model;

/**
 * Describes requested state of a favorited item
 *
 * @author Ivan Meredith
 * @author Vivek Pandey
 *
 * @see io.jenkins.blueocean.rest.model.BluePipeline#favorite(BlueFavoriteAction)
 */
public class BlueFavoriteAction {
    private Boolean favorite;

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @return true favorite, false un-favorite, null toggle
     */
    public Boolean isFavorite() {
        return favorite;
    }
}
