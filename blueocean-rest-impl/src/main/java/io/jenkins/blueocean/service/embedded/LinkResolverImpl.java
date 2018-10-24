package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link LinkResolver}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
@Extension
public class LinkResolverImpl extends LinkResolver {

    private final Logger logger = LoggerFactory.getLogger(LinkResolverImpl.class);

    @Override
    public Link resolve(Object modelObject) {
        if (modelObject instanceof Job) {
            Resource resource =  resolveJob((Job)modelObject);
            if(resource != null){
                return resource.getLink();
            }
        }else if(modelObject instanceof Item && modelObject instanceof ItemGroup){
            Resource resource = resolveFolder((Item) modelObject);
            if(resource!=null){
                return resource.getLink();
            }
        }else if(modelObject instanceof Run){
            Run run = (Run) modelObject;
            Resource resource = resolveRun(run);
            if(resource != null){
                return resource.getLink();
            }
        }
        return null;
    }

    private Resource resolveJob(Job job){
        return BluePipelineFactory.resolve(job);
    }

    private Resource resolveFolder(Item folder){
        return BluePipelineFactory.resolve(folder);
    }

    private Resource resolveRun(Run run){
        Resource resource = resolveJob(run.getParent());
        if(resource instanceof BluePipeline){
            BluePipeline pipeline = (BluePipeline) resource;
            BlueRunContainer blueRunContainer = pipeline.getRuns();
            return blueRunContainer == null ? null : blueRunContainer.get(run.getId());
        }
        return null;
    }

}
