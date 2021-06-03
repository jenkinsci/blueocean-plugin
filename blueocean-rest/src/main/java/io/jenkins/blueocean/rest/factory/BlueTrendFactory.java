package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.commons.IterableUtils;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTrend;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Allows extensions to attach {@link BlueTrend} data to a {@link BluePipeline} for reports and visualization.
 *
 * @see BlueTrend
 * @author cliffmeyers
 */
public abstract class BlueTrendFactory implements ExtensionPoint {

    public abstract BlueTrend getTrend(BluePipeline pipeline, Link parent);

    public static Iterable<BlueTrend> getTrends(final BluePipeline pipeline, final Link parent) {

        Stream<BlueTrend> stream = ExtensionList.lookup(BlueTrendFactory.class).stream()
            .map(blueTrendFactory -> blueTrendFactory != null ? blueTrendFactory.getTrend(pipeline, parent) : null)
            .filter(Objects::nonNull);
        return IterableUtils.getIterable(stream);
    }
}
