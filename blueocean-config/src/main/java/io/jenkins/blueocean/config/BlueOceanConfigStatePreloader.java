package io.jenkins.blueocean.config;

import hudson.Extension;
import hudson.Plugin;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.SecurityRealm;
import hudson.util.VersionNumber;
import io.jenkins.blueocean.auth.jwt.JwtTokenServiceEndpoint;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.PageStatePreloader;
import io.jenkins.blueocean.rest.factory.BlueOceanConfigFactory;
import io.jenkins.blueocean.rest.model.BlueOceanConfig;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONBuilder;

import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * @author Vivek Pandey
 */
@Extension
public class BlueOceanConfigStatePreloader extends PageStatePreloader {

    private static final Logger LOGGER = Logger.getLogger(BlueOceanConfigStatePreloader.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatePropertyPath() {
        return "config";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateJson() {
        StringWriter writer = new StringWriter();
        Jenkins jenkins = Jenkins.getInstance();
        VersionNumber versionNumber = Jenkins.getVersion();
        String version = versionNumber != null ? versionNumber.toString() : Jenkins.VERSION;

        AuthorizationStrategy authorizationStrategy = jenkins.getAuthorizationStrategy();
        boolean allowAnonymousRead = true;
        if(authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy){
            allowAnonymousRead = ((FullControlOnceLoggedInAuthorizationStrategy) authorizationStrategy).isAllowAnonymousRead();
        }

        String jwtTokenEndpointHostUrl = Jenkins.getInstance().getRootUrl();
        JwtTokenServiceEndpoint jwtTokenServiceEndpoint = JwtTokenServiceEndpoint.first();
        if(jwtTokenServiceEndpoint != null){
            jwtTokenEndpointHostUrl = jwtTokenServiceEndpoint.getHostUrl();
        }
        addFeatures(new JSONBuilder(writer)
            .object()
                .key("version").value(getBlueOceanPluginVersion())
                .key("jenkinsConfig")
                .object()
                    .key("version").value(version)
                    .key("security")
                    .object()
                        .key("enabled").value(jenkins.isUseSecurity())
                        .key("loginUrl").value(jenkins.getSecurityRealm() == SecurityRealm.NO_AUTHENTICATION ? null : jenkins.getSecurityRealm().getLoginUrl())
                        .key("authorizationStrategy").object()
                            .key("allowAnonymousRead").value(allowAnonymousRead)
                        .endObject()
                        .key("enableJWT").value(BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION)
                        .key("jwtServiceHostUrl").value(jwtTokenEndpointHostUrl)
                    .endObject()
                .endObject()
                ) // addFeatures here
            .endObject();

        return writer.toString();
    }
    
    /**
     * Adds all features from all BlueOceanConfig objects
     * @param builder JSON builder to use
     * @return same JSON builder
     */
    private JSONBuilder addFeatures(JSONBuilder builder) {
        JSONBuilder features = builder.key("features").object();
        for (BlueOceanConfigFactory factory : BlueOceanConfigFactory.all()) {
            BlueOceanConfig config = factory.getConfig();
            for (String key : config.keys()) {
                features.key(key).value(config.get(key, Object.class));
            }
        }
        return features.endObject();
    }

    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    private String getBlueOceanPluginVersion(){
        Plugin plugin = Jenkins.getInstance().getPlugin("blueocean-web");
        if(plugin == null) {
            return null;
        }
        return plugin.getWrapper().getVersion();
    }
}
