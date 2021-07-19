package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.UsageStatistics;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory;
import jenkins.model.Jenkins;

/**
 * Adds 'Open Blue Ocean' button on the left side bar of classic Jenkins UI.
 *
 * @author Vivek Pandey
 */
@Extension
public final class BlueOceanRootActionUrl implements RootAction {
    private volatile BlueOceanUrlObjectFactory factory;

    @Override
    public String getIconFileName() {
        setBlueOceanUIProvider();
        if(factory != null){
            return factory.get(Jenkins.get()).getIconUrl();
        }
        return "/plugin/blueocean-rest-impl/images/48x48/blueocean.png";
    }

    @Override
    public String getDisplayName() {
        setBlueOceanUIProvider();
        if(factory != null){
            return factory.get(Jenkins.get()).getDisplayName();
        }
        return Messages.BlueOceanUrlAction_DisplayName();
    }

    @Override
    public String getUrlName() {
        setBlueOceanUIProvider();
        if(factory != null){
            return factory.get(Jenkins.get()).getUrl();
        }
        return BlueOceanUrlMapperImpl.getLandingPagePath();
    }

    public boolean isAnalyticsEnabled() {
        return Analytics.isAnalyticsEnabled();
    }

    //lazy initialization in thread safe way
    private void setBlueOceanUIProvider(){
        BlueOceanUrlObjectFactory f = factory;
        if(f == null){
            synchronized (this){
                f = factory;
                if(f == null){
                    for(BlueOceanUrlObjectFactory b: BlueOceanUrlObjectFactory.all()){
                        factory = f = b;
                        return;
                    }
                }
            }
        }
    }
}
