package io.jenkins.blueocean.service.embedded;

import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BlueOceanUrlObjectImpl extends BlueOceanUrlObject {

    private volatile String mappedUrl;
    private final ModelObject modelObject;

    public BlueOceanUrlObjectImpl(ModelObject modelObject) {
        this.modelObject = modelObject;
    }

    @Override
    public @Nonnull String getDisplayName() {
        return Messages.BlueOceanUrlAction_DisplayName();
    }

    @Override
    public @Nonnull String getUrl() {
        setUrlIfNeeded();
        return mappedUrl;
    }

    @Override
    public @Nonnull String getIconUrl() {
        return "/plugin/blueocean-rest-impl/images/48x48/blueocean.png";
    }

    private void setUrlIfNeeded(){
        String url = mappedUrl;
        if(url == null){
            synchronized (this){
                url = mappedUrl;
                if(url == null){
                    url = computeUrl();
                    this.mappedUrl = url;
                }
            }
        }
    }

    private String computeUrl(){
        String url = null;
        for(BlueOceanUrlMapper mapper: BlueOceanUrlMapper.all()){
            url = mapper.getUrl(modelObject);
            if(url != null){
                break;
            }
        }
        if(url == null){
            url = BlueOceanUrlMapperImpl.getLandingPagePath();
        }
        return url;
    }
}
