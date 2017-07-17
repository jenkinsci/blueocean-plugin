package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

@Restricted(NoExternalUse.class)
public class BlueTrendContainerImpl extends BlueTrendContainer {

    private final BluePipeline pipeline;

    public BlueTrendContainerImpl(BluePipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public Link getLink() {
        return pipeline.getLink().rel("trends");
    }

    @Override
    public BlueTrend get(final String name) {
        BlueTrend trend = Iterators.find(iterator(), new Predicate<BlueTrend>() {
            @Override
            public boolean apply(@Nullable BlueTrend input) {
                return input != null && input.getId().equals(name);
            }
        }, null);
        if (trend == null) {
            throw new ServiceException.NotFoundException("not found");
        }
        return trend;
    }

    @Override
    @Nonnull
    public Iterator<BlueTrend> iterator() {
        return BlueTrendFactory.getTrends(pipeline, getLink()).iterator();
    }
}
