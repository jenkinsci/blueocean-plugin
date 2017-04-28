package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import io.jenkins.blueocean.auth.jwt.JwtTokenServiceEndpoint;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal=-9999)
public class JwtTokenServiceEndpointImpl extends JwtTokenServiceEndpoint{

    @Nonnull @Override
    @SuppressWarnings("ConstantConditions")
    public String getHostUrl() {
        return Jenkins.getInstance().getRootUrl();
    }
}
