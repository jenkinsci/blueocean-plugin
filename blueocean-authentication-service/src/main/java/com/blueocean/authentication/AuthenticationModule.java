package com.blueocean.authentication;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.blueocean.api.authentication.AuthenticationService;

@Extension
public class AuthenticationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthenticationService.class).to(AuthenticationServiceImpl.class).asEagerSingleton();
    }
}
