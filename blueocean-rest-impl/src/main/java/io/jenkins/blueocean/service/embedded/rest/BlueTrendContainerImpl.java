package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

// TODO: reenable this after refactor
// @Restricted(NoExternalUse.class)
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
        Optional<BlueTrend> trend = StreamSupport.stream( list().spliterator(), false).
            filter( blueTrend -> blueTrend != null && blueTrend.getId().equals(name)).
            findFirst();

        if (!trend.isPresent()) {
            throw new ServiceException.NotFoundException("not found");
        }
        return trend.get();
    }

    @Override
    @NonNull
    public Iterator<BlueTrend> iterator() {
        return BlueTrendFactory.getTrends(pipeline, getLink()).iterator();
    }
}
