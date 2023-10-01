package io.jenkins.blueocean.service.embedded;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class BlueOceanUrlObjectImpl extends BlueOceanUrlObject {

    private final String mappedUrl;

    // leave it there to avoid deserialization errors for older version of this object
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "Field is present to avoid deserialization errors for older version of this object")
    private transient ModelObject modelObject;

    public BlueOceanUrlObjectImpl(ModelObject modelObject) {
        this.mappedUrl = computeUrl(modelObject);
    }

    @Override
    public @NonNull String getDisplayName() {
        return Messages.BlueOceanUrlAction_DisplayName();
    }

    @Override
    public @NonNull String getUrl() {
        return mappedUrl;
    }

    @Override
    public @NonNull String getIconUrl() {
        return "/plugin/blueocean-rest-impl/images/48x48/blueocean.png";
    }

    private String computeUrl(ModelObject modelObject){
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
