package io.jenkins.blueocean.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.io.Resources;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@Singleton
final class Features {

    private static final String BLUEOCEAN_FEATURES_PROPERTIES = "io.jenkins.blueocean.config/features.properties";

    private final Supplier<ImmutableMap<String, String>> featureSupplier = Suppliers.memoize(new Supplier<ImmutableMap<String, String>>() {
        @Override
        public ImmutableMap<String, String> get() {
            // Find the default list of properties
            final URL url = Resources.getResource(BLUEOCEAN_FEATURES_PROPERTIES);
            final Properties properties = new Properties();
            try {
                try (InputStream inputStream = url.openStream()) {
                    properties.load(inputStream);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not load " + BLUEOCEAN_FEATURES_PROPERTIES);
            }
            // Allow features to be enabled via system property or environment variable
            return ImmutableMap.copyOf(Maps.transformEntries(Maps.fromProperties(properties), new EntryTransformer<String, String, String>() {
                @Override
                public String transformEntry(String key, String value) {
                    String featureKey = "feature." + key;
                    String featureValue = System.getenv(featureKey.toUpperCase().replace('.', '_'));
                    return featureValue == null ? System.getProperty(featureKey, value) : featureValue;
                }
            }));
        }
    });

    public String getFeature(String featureName) {
        return get().get(featureName);
    }

    ImmutableMap<String, String> get() {
        return featureSupplier.get();
    }
}
