package io.jenkins.blueocean.service.embedded;

import hudson.util.XStream2;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;

import static org.junit.Assert.assertThat;

public class BlueOceanUrlActionTest {

    @Test
    public void testMigration() {
        BlueOceanUrlObject urlObject = new BlueOceanUrlObjectImpl(null);

        BlueOceanUrlAction original = new BlueOceanUrlAction(urlObject);
        XStream2 xs = new XStream2();
        String s = xs.toXML(original);
        Object result = xs.fromXML(s);
        assertThat(result, instanceOf(BlueOceanUrlAction.DoNotShowPersistedBlueOceanUrlActions.class));
    }
}
