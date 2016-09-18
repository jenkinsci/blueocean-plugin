package io.jenkins.blueocean.config;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.SecurityRealm;
import io.jenkins.blueocean.BluePageDecorator;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONBuilder;

import java.io.StringWriter;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = 10)
public class BlueOceanConfig extends BluePageDecorator {

    @Inject
    Features features;

    public boolean isRollBarEnabled(){
        return BlueOceanConfigProperties.ROLLBAR_ENABLED;
    }

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

        JSONBuilder builder = new JSONBuilder(writer)
            .object()
                .key("version").value(getBlueOceanPluginVersion())
                .key("jenkinsConfig")
                .object()
                    .key("version").value(version)
                    .key("security")
                    .object()
                        .key("enabled").value(jenkins.isUseSecurity())
                        .key("loginUrl").value(jenkins.getSecurityRealm() == SecurityRealm.NO_AUTHENTICATION ? null : jenkins.getSecurityRealm().getLoginUrl())
                        .key("user").value(Jenkins.getAuthentication().getName())
                        .key("authorizationStrategy").object()
                            .key("allowAnonymousRead").value(allowAnonymousRead)
                            .endObject()
                        .key("enableJWT").value(BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION)
                        .endObject();
                        features(builder)
                    .endObject()
                .endObject();

        return writer.toString();
    }

    private JSONBuilder features(JSONBuilder builder) {
        builder = builder.key("features").object();
        for (Map.Entry<String, String> entry : features.get().entrySet()) {
            builder.key(entry.getKey());
            builder.value(entry.getValue());
        }
        return builder.endObject();
    }

    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    private String getBlueOceanPluginVersion(){
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }

}
