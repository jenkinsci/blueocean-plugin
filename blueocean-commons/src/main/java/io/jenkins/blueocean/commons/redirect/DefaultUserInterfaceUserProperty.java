package io.jenkins.blueocean.commons.redirect;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.util.List;


public class DefaultUserInterfaceUserProperty extends UserProperty {

    @Nullable
    private String interfaceId;

    @Nullable
    public String getInterfaceId() {
        return interfaceId;
    }

    @DataBoundConstructor
    public DefaultUserInterfaceUserProperty(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public static List<InterfaceOption> allInterfaces() {
        return ImmutableList.of(InterfaceOption.classic, InterfaceOption.blueocean);
    }

    public boolean isSelected(String interfaceId) {
        return interfaceId.equals(this.getInterfaceId());
    }

    @Extension
    public static final UserPropertyDescriptor DESCRIPTOR = new DefaultUserInterfaceUserPropertyDescriptor();


}
