package io.jenkins.blueocean.service.embedded;

import io.jenkins.blueocean.auth.jwt.JwtTokenServiceEndpoint;
import jenkins.model.Jenkins;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Pandey
 */
public class JwtTokenServiceEndpointImplTest extends BaseTest{
    @Test
    public void verify(){
        List<JwtTokenServiceEndpoint>   jwtTokenServiceEndpoints = JwtTokenServiceEndpoint.all();
        assertEquals(1, jwtTokenServiceEndpoints.size());
        assertEquals(Jenkins.getInstance().getRootUrl(), jwtTokenServiceEndpoints.get(0).getHostUrl());
    }
}
