package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import jenkins.model.Jenkins;
import jenkins.model.lazy.LazyBuildMixIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Search API for Run
 *
 * @author Vivek Pandey
 */
@Extension
public class RunSearch extends OmniSearch<BlueRun> {

    private static final Logger LOGGER = LoggerFactory.getLogger( RunSearch.class );

    @Override
    public String getType() {
        return "run";
    }

    @Override
    public Pageable<BlueRun> search(Query q) {

        String pipeline = q.param("pipeline", false);

        boolean latestOnly = q.param("latestOnly", Boolean.class);

        if(pipeline != null){
            TopLevelItem p = Jenkins.getActiveInstance().getItem(pipeline);
            if(latestOnly){
                BlueRun r = getLatestRun((Job)p);
                if(r != null) {
                    return Pageables.wrap(Collections.singletonList(r));
                }else{
                    Pageables.empty();
                }
            }
            if (p instanceof Job) {
                return Pageables.wrap(findRuns((Job)p));
            }else{
                throw new ServiceException.BadRequestException(String.format("Pipeline %s not found", pipeline));
            }
        }else if(latestOnly){
            return Pageables.empty();
        }
        return Pageables.wrap(findRuns(null));
    }

    public static Iterable<BlueRun> findRuns(Job job, final Link parent){
        final List<BlueRun> runs = new ArrayList<>();
        Iterable<Job> pipelines;
        if(job != null){
            pipelines = ImmutableList.of(job);
        }else{
            pipelines = Jenkins.getInstance().getItems(Job.class);
        }
        for (Job p : pipelines) {
            RunList<? extends Run> runList = p.getBuilds();

            for (Run r : runList) {
                BlueRun run = BlueRunFactory.getRun(r, () -> parent);
                if (run != null) {
                    runs.add(run);
                }
            }
        }

        return runs;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<BlueRun> findRuns(Job job, final Link parent, int start, int limit){
        final List<BlueRun> runs = new ArrayList<>();
        Iterable<Job> pipelines;
        if(job != null){
            pipelines = ImmutableList.of(job);
        }else{
            pipelines = Jenkins.getInstance().getItems(Job.class);
        }

        for (Job p : pipelines) {
            Iterator<? extends Run> runIterator;
            if (job instanceof LazyBuildMixIn.LazyLoadingJob) {
                final LazyBuildMixIn lazyLoadMixin = ((LazyBuildMixIn.LazyLoadingJob) job).getLazyBuildMixIn();
                runIterator = lazyLoadMixin.getRunMap().iterator();

            }else{
                runIterator = p.getBuilds().iterator();

            }
            runs.addAll(collectRuns(runIterator, parent, start, limit));
        }

        return runs;
    }

    public static final String COLLECT_THREADS_KEY = "blueocean.collectRuns.threads";

    private static final int COLLECT_THREADS = Integer.getInteger( COLLECT_THREADS_KEY, 0 );

    private static List<BlueRun> collectRuns(Iterator<? extends Run> runIterator, final Link parent, int start, int limit){
        if (COLLECT_THREADS > 1) {
            LOGGER.debug( "collectRunsParallel {}", COLLECT_THREADS );
            return collectRunsParallel( runIterator, parent, start, limit );
        }
        return collectRunsSingleThread( runIterator, parent, start, limit );
    }

    private static List<BlueRun> collectRunsSingleThread(Iterator<? extends Run> runIterator, final Link parent, int start, int limit){
        List<BlueRun> runs = new ArrayList<>();
        int skipCount = start; // Skip up to the start
        while (runIterator.hasNext()) {
            if (skipCount > 0) {
                runIterator.next();
                skipCount--;
            } else {
                Run r = runIterator.next();
                BlueRun run = BlueRunFactory.getRun(r, () ->  parent);
                if (run != null) {
                    runs.add(run);
                }
            }
            if (runs.size() >= limit) {
                return runs;
            }
        }
        return runs;
    }


    private static List<BlueRun> collectRunsParallel(Iterator<? extends Run> runIterator, final Link parent, int start, int limit){

        List<Callable<BlueRun>> callables = new CopyOnWriteArrayList<>();

        int skipCount = start; // Skip up to the start
        while (runIterator.hasNext()) {
            if (skipCount > 0) {
                runIterator.next();
                skipCount--;
            } else {
                Run r = runIterator.next();
                callables.add(() -> BlueRunFactory.getRun(r, () ->  parent));
            }
            if (callables.size() >= limit) {
                break;
            }
        }
        int n = callables.size();
        LOGGER.debug( "before submit size:{}", n );
        if(n<1){
            return Collections.emptyList();
        }
        ExecutorService
            executorService =  new ThreadPoolExecutor( n < COLLECT_THREADS? n : COLLECT_THREADS,
                                                       n < COLLECT_THREADS? n : COLLECT_THREADS,
                                                       60L, TimeUnit.MILLISECONDS,
                                                       new ArrayBlockingQueue<>( n ));
        ExecutorCompletionService<BlueRun> ecs = new ExecutorCompletionService( executorService );
        for(Callable<BlueRun> callable : callables) {
            ecs.submit( callable );
        }
        LOGGER.debug( "submit done size:{}", n );
        List<BlueRun> runs = new ArrayList<>( n );
        try
        {
            for (int i = 0; i < n; ++i) {
                BlueRun r = ecs.take().get();
                if (r != null) {
                    runs.add( r );
                }
            }
        } catch ( InterruptedException e ) {
            LOGGER.error( e.getMessage(), e );
        } catch ( ExecutionException e ) {
            LOGGER.error( e.getMessage(), e );
        } finally {
            executorService.shutdownNow();
        }
        LOGGER.debug( "runs found:{}", runs.size() );
        return runs;
    }

    public static Iterable<BlueRun> findRuns(Job pipeline){
        return findRuns(pipeline, null);
    }

    private BlueRun getLatestRun(Job job){
        if (job == null) {
            return null;
        }
        BlueOrganization org = OrganizationFactory.getInstance().getContainingOrg(job);
        if (org == null) {
            return null;
        }
        Run r = job.getLastBuild();
        if (r == null) {
            return null;
        }
        Resource resource = BluePipelineFactory.resolve(job);
        if (resource == null) {
            return null;
        }
        return BlueRunFactory.getRun(r, resource);
    }

}
