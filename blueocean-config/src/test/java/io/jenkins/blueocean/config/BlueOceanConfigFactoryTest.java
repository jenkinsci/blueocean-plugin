package io.jenkins.blueocean.config;

import com.google.common.collect.ImmutableSet;
import io.jenkins.blueocean.rest.factory.BlueOceanConfigFactory;
import io.jenkins.blueocean.rest.model.BlueOceanConfig;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlueOceanConfigFactoryTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void smokesTest() {
        assertThat(BlueOceanConfigFactory.getConfig(BlueOceanConfig.ORGANIZATION_ENABLED, Boolean.class), notNullValue());
    }

    @Test
    public void extensionCanOverrideBehaviourTest() throws Exception {

        BlueOceanTestConfigurationFactory.setValue = null; //do not override value

        Boolean originalConfig = BlueOceanConfigFactory.getConfig(BlueOceanConfig.ORGANIZATION_ENABLED, Boolean.class);

        assertThat("Overridden extension point should have been called", BlueOceanTestConfigurationFactory.loaded, equalTo(true));

        //Check reload and override
        BlueOceanTestConfigurationFactory.setValue = !originalConfig;
        BlueOceanTestConfigurationFactory.loaded = false;

        Boolean overriddenConfig = BlueOceanConfigFactory.getConfig(BlueOceanConfig.ORGANIZATION_ENABLED, Boolean.class);

        assertThat("Configuration should have been reloaded", BlueOceanTestConfigurationFactory.loaded, equalTo(true));
        assertThat("Configuration should have been overridden", originalConfig, not(equalTo(overriddenConfig)));

    }

    @TestExtension(value = "extensionCanOverrideBehaviourTest")
    public static class BlueOceanTestConfigurationFactory extends BlueOceanConfigFactory {
        static boolean loaded = false;
        static Boolean setValue;

        @Override
        public BlueOceanConfig getConfig() {
            final Boolean value = setValue;
            loaded = true;
            return new BlueOceanConfig() {
                @Override
                public Iterable<String> keys() {
                    return ImmutableSet.of(BlueOceanConfig.ORGANIZATION_ENABLED);
                }
                
                @Override
                public <T> T get(String key, Class<T> type) {
                    if (key.equals(BlueOceanConfig.ORGANIZATION_ENABLED)) {
                        if (value != null) {
                            return (T) value;
                        }
                        return (T) Boolean.FALSE;
                    }
                    return null;
                }
            };
        }
    }
}
