package io.jenkins.blueocean.config;

import hudson.Extension;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import io.jenkins.blueocean.BluePageDecorator;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONBuilder;

import java.io.StringWriter;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = 10)
public class BlueOceanConfig extends BluePageDecorator {

    public boolean isRollBarEnabled(){
        return Boolean.getBoolean("BLUEOCEAN_ROLLBAR_ENABLED");
    }
    public final boolean FEATURE_JWT = Boolean.getBoolean("FEATURE_BLUEOCEAN_JWT_AUTHENTICATION");


    public String getBlueOceanConfig(){
        return createConfig();
    }

    private String createConfig() {
        Jenkins jenkins = Jenkins.getInstance();
        String version = Jenkins.getVersion() != null ? Jenkins.getVersion().toString() : Jenkins.VERSION;
        StringWriter writer = new StringWriter();

        AuthorizationStrategy authorizationStrategy = jenkins.getAuthorizationStrategy();
        boolean allowAnonymousRead = true;
        if(authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy){
            allowAnonymousRead = ((FullControlOnceLoggedInAuthorizationStrategy) authorizationStrategy).isAllowAnonymousRead();
        }

        new JSONBuilder(writer)
            .object()
                .key("version").value(getBlueOceanPluginVersion())
                .key("jenkinsConfig")
                .object()
                    .key("version").value(version)
                    .key("security")
                    .object()
                        .key("enabled").value(jenkins.isUseSecurity())
                        .key("authorizationStrategy").object()
                            .key("allowAnonymousRead").value(allowAnonymousRead)
                            .endObject()
                        .key("enableJWT").value(FEATURE_JWT)
                        .endObject()
                    .endObject()
                .endObject();

        return writer.toString();
    }

    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    private String getBlueOceanPluginVersion(){
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }

}
