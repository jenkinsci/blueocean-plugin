package io.jenkins.blueocean.service.embedded;

import hudson.util.XStream2;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class BlueOceanUrlActionTest {

    @Test
    public void testMigration() {
        BlueOceanUrlObject mock = mock(BlueOceanUrlObject.class);

        BlueOceanUrlAction original = new BlueOceanUrlAction(mock);
        XStream2 xs = new XStream2();
        String s = xs.toXML(original);
        Object result = xs.fromXML(s);
        assertThat(result, instanceOf(BlueOceanUrlAction.DoNotShowPersistedBlueOceanUrlActions.class));
    }
}
