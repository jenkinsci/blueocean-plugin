package io.jenkins.blueocean.rest.factory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTrend;

import javax.annotation.Nullable;

public abstract class BlueTrendFactory implements ExtensionPoint {

    public abstract BlueTrend getTrend(BluePipeline pipeline, Link parent);

    public static Iterable<BlueTrend> getTrends(final BluePipeline pipeline, final Link parent) {
        return Iterables.filter(Iterables.transform(ExtensionList.lookup(BlueTrendFactory.class), new Function<BlueTrendFactory, BlueTrend>() {
            @Override
            public BlueTrend apply(@Nullable BlueTrendFactory factory) {
                return factory != null ? factory.getTrend(pipeline, parent) : null;
            }
        }), Predicates.notNull());
    }
}
