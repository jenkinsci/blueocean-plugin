package io.jenkins.blueocean.service.embedded.redirect;

import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

import javax.annotation.Nonnull;

public class DefaultUserInterfaceUserPropertyDescriptor extends UserPropertyDescriptor {

    public DefaultUserInterfaceUserPropertyDescriptor() {
        super(DefaultUserInterfaceUserProperty.class);
    }

    @Override
    public UserProperty newInstance(User user) {
        return new DefaultUserInterfaceUserProperty(DefaultUserInterfaceUserProperty.system.getInterfaceId());
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Default User Interface";
    }

}
