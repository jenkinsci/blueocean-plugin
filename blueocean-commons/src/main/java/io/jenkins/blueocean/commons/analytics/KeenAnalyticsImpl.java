package io.jenkins.blueocean.commons.analytics;

import io.keen.client.java.JavaKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Map;
import java.util.logging.Logger;

@Restricted(NoExternalUse.class)
public class KeenAnalyticsImpl extends Analytics {

    static final Analytics INSTANCE = new KeenAnalyticsImpl();

    private static final Logger LOGGER = Logger.getLogger(KeenAnalyticsImpl.class.getName());
    private static final String PROJECT_ID = "5950934f3d5e150f5ab9d7be";
    private static final String WRITE_KEY = "E6C1FA3407AF4DD3115DBC186E40E9183A90069B1D8BBA78DB3EA6B15EA6182C881E8C55B4D7A48F55D5610AD46F36E65093227A7490BF7A56307047903BCCB16D05B9456F18A66849048F100571FDC91888CAD94F2A271A8B9E5342D2B9404E";
    private final KeenClient client;

    private KeenAnalyticsImpl() {
        this.client = new JavaKeenClientBuilder().build();
        KeenClient.initialize(this.client);
        KeenClient.client().setDefaultProject(new KeenProject(PROJECT_ID, WRITE_KEY, null));
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        client.addEvent(name, allProps);
    }
}
