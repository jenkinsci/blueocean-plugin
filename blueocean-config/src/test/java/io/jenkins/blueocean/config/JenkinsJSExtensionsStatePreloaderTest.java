package io.jenkins.blueocean.config;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class JenkinsJSExtensionsStatePreloaderTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void getStatePropertyPath() {
        assertThat(new JenkinsJSExtensionsStatePreloader().getStatePropertyPath(), equalTo("jsExtensions"));
    }

    @Test
    public void assureStateJsonIsValidJson() {
        final String stateJson = new JenkinsJSExtensionsStatePreloader().getStateJson();

        final JSONArray jsonArray = JSONArray.fromObject(stateJson);

        final Optional<JSONObject> plugin = jsonArray.stream()
            .map(o -> (JSONObject)o)
            .filter(o -> o.getString("hpiPluginId").equals("blueocean-commons"))
            .findFirst();
        assertThat(plugin.isPresent(),is(true));
        assertThat(plugin.get().getString("hpiPluginVer"),notNullValue());
    }
}
