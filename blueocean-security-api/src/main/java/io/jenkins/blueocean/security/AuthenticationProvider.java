package io.jenkins.blueocean.security;

import javax.annotation.Nonnull;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

public interface AuthenticationProvider {

    String getLoginUrl();

    @Nonnull
    <T extends LoginDetails> LoginDetailsProvider<T> getLoginDetailsProvider();
}
