package io.jenkins.blueocean.config;

import hudson.Extension;
import hudson.model.User;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.SecurityRealm;
import io.jenkins.blueocean.BluePageDecorator;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.stapler.ModelObjectSerializer;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONBuilder;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = 10)
public class BlueOceanConfig extends BluePageDecorator {

    public boolean isRollBarEnabled(){
        return BlueOceanConfigProperties.ROLLBAR_ENABLED;
    }

    public String getBlueOceanUser() throws IOException {
        try (StringWriter writer = new StringWriter()) {
            User currentUser = User.current();
            JSONObject currentUserJson;

            if (currentUser != null) {
                currentUserJson = JSONObject.fromObject(ModelObjectSerializer.toJson(new UserImpl(currentUser)));
            } else {
                currentUserJson = new JSONObject();
                currentUserJson.put("id", "anonymous");
            }

            new JSONBuilder(writer)
                .object()
                    .key("user").value(currentUserJson)
                .endObject();

            return writer.toString();
        }
    }

    public String getBlueOceanConfig() throws IOException {
        try (StringWriter writer = new StringWriter()) {
            Jenkins jenkins = Jenkins.getInstance();
            String version = Jenkins.getVersion() != null ? Jenkins.getVersion().toString() : Jenkins.VERSION;

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
                            .key("loginUrl").value(jenkins.getSecurityRealm() == SecurityRealm.NO_AUTHENTICATION ? null : jenkins.getSecurityRealm().getLoginUrl())
                            .key("authorizationStrategy").object()
                                .key("allowAnonymousRead").value(allowAnonymousRead)
                                .endObject()
                            .key("enableJWT").value(BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION)
                            .endObject()
                        .endObject()
                    .endObject();

            return writer.toString();
        }
    }

    public String getJsExtensions() {
        return JenkinsJSExtensions.getExtensionsData().toString();
    }

    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    private String getBlueOceanPluginVersion(){
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }

}
