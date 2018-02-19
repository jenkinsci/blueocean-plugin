package io.jenkins.blueocean.service.embedded.redirect;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.util.List;

import static io.jenkins.blueocean.service.embedded.redirect.InterfaceOption.blueocean;
import static io.jenkins.blueocean.service.embedded.redirect.InterfaceOption.classic;

@Extension
public class DefaultUserInterfaceGlobalConfiguration extends GlobalConfiguration {

    @Nullable
    private String interfaceId;

    @Nullable
    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(@Nullable String interfaceId) {
        this.interfaceId = interfaceId;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public static List<InterfaceOption> allInterfaces() {
        return ImmutableList.of(classic, blueocean);
    }

    public boolean isSelected(String interfaceId) {
        return interfaceId.equals(this.getInterfaceId());
    }
}
