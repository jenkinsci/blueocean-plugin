package io.jenkins.blueocean.config;

import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class BlueOceanConfigStatePreloaderTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void getStatePropertyPath() {
        assertThat(new BlueOceanConfigStatePreloader().getStatePropertyPath(), equalTo("config"));
    }

    @Test
    public void assureStateJsonIsValidJson() {
        final String stateJson = new BlueOceanConfigStatePreloader().getStateJson();

        final JSONObject jsonObject = JSONObject.fromObject(stateJson);

        assertThat(jsonObject.getString("version"), notNullValue());
        assertThat(jsonObject.getJSONObject("jenkinsConfig"), notNullValue());
        assertThat(jsonObject.getJSONObject("jenkinsConfig").getBoolean("analytics"), is(false));
    }
}
