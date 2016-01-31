package io.jenkins.blueocean.rest;

import com.google.inject.Guice;
import com.google.inject.Inject;
import hudson.Extension;
import io.jenkins.blueocean.rest.guice.MainModule;
import io.jenkins.blueocean.rest.profile.ProfileService;
import io.jenkins.embryo.App;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Root of REST API
 *
 * @author Vivek Pandey
 **/
@Extension
public class ApiHead extends App {
    public ApiHead() {
        Guice.createInjector(new MainModule()).injectMembers(this);
    }

    @Inject
    private ProfileService.StandaloneProfileService profileService;


    public Object doDynamics(StaplerRequest req, StaplerResponse resp, Object node){
        return profileService.findOrganizations();
    }

    public Object doOrganizations(){
        return profileService.findOrganizations();
    }

}
