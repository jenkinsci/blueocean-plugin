package io.jenkins.blueocean.service.embedded;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.blueocean.auth.jwt.JwtTokenServiceEndpoint;
import jenkins.model.Jenkins;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal=-9999)
public class JwtTokenServiceEndpointImpl extends JwtTokenServiceEndpoint{

    @NonNull @Override
    @SuppressWarnings("ConstantConditions")
    public String getHostUrl() {
        return Jenkins.getInstance().getRootUrl();
    }
}
