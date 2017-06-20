package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.Item;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import io.jenkins.blueocean.service.embedded.util.GlobMatcher;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Returns flattened view of pipelines
 *
 * To exclude flattening multi branch project:
 *
 * GET /rest/search/?q=type:pipeline;organization:jenkins;excludedFromFlattening=jenkins.branch.MultiBranchProject
 *
 * To exclude flattening a folder:
 *
 * GET /rest/search/?q=type:pipeline;organization:jenkins;excludedFromFlattening=com.cloudbees.hudson.plugins.folder.AbstractFolder
 *
 * To exclude flattening both a folder and multi-branch projects
 *
 * GET /rest/search/?q=type:pipeline;organization:jenkins;excludedFromFlattening=jenkins.branch.MultiBranchProject,com.cloudbees.hudson.plugins.folder.AbstractFolder
 *
 *
 * @author Vivek Pandey
 */
@Extension
public class PipelineSearch extends OmniSearch<BluePipeline>{
    private static final String EXCLUDED_FROM_FLATTENING_PARAM ="excludedFromFlattening";
    private static final String ORGANIZATION_PARAM="organization";

    private static final Logger logger = LoggerFactory.getLogger(PipelineSearch.class);

    @Override
    public String getType() {
        return "pipeline";
    }

    @Override
    public Pageable<BluePipeline> search(Query q) {
        String s = q.param(EXCLUDED_FROM_FLATTENING_PARAM);
        ItemGroup org = org(q);

        List<Class> excludeList=new ArrayList<>();
        if(s!=null){
            for(String s1:s.split(",")){
                Class c = null;
                try {
                    c = Class.forName(s1);
                } catch (ClassNotFoundException e) {
                    try {
                        //TODO: There should be better ways to find a class from a plugin.
                        Plugin p = Jenkins.getInstance().getPlugin("blueocean-pipeline-api-impl");
                        if(p != null){
                            c = p.getWrapper().classLoader.loadClass(s1);
                        }else{
                            logger.error("blueocean-pipeline-api-impl plugin not found!");
                        }
                    } catch (ClassNotFoundException e1) {
                        logger.error(e.getMessage(), e1);
                    }
                    //ignored, give other OmniSearch implementations chance, they might handle it
                    //throw new ServiceException.BadRequestException(String.format("%s parameter has invalid value: %s", EXCLUDED_FROM_FLATTENING_PARAM, s1), e);
                }
                if(c!=null){
                    excludeList.add(c);
                }
            }
        }

        Collection<Item> items = new ArrayList<>();
        if(!excludeList.isEmpty()) {
            for (Item item : getAllItems(org)) {
                if (!exclude(item.getParent(), excludeList)) {
                    items.add(item);
                }
            }
        }else{
            items = getAllItems(org);
        }
        items = ContainerFilter.filter(items);
        final Iterator<BluePipeline> pipelineIterator = new PipelineContainerImpl(org)
            .getPipelines(items);
        final List<BluePipeline> pipelines = new ArrayList<>();
        String pipeline = q.param(getType());
        if(pipeline == null) {
            return Pageables.wrap(new Iterable<BluePipeline>() {
                @Override
                public Iterator<BluePipeline> iterator() {
                    return pipelineIterator;
                }
            });
        }else{
            GlobMatcher matcher = pipeline.contains("*") ? new GlobMatcher(pipeline) : null;
            if (matcher != null) {
                while (pipelineIterator.hasNext()) {
                    BluePipeline p = pipelineIterator.next();
                    String decodedName = null;
                    try {
                        decodedName = URLDecoder.decode(p.getFullDisplayName(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new UnexpectedErrorException("Could not decode '" + p.getFullDisplayName() + "'", e);
                    }
                    if (matcher.matches(decodedName)) {
                        pipelines.add(p);
                    }
                }

            } else {
                while (pipelineIterator.hasNext()) {
                    BluePipeline p = pipelineIterator.next();
                    if (pipeline.equals(p.getFullDisplayName())) {
                        pipelines.add(p);
                    }
                }
            }
            return Pageables.wrap(pipelines);
        }
    }

    private List<Item> getAllItems(ItemGroup org) {
        List<Item> r = new ArrayList<>();
        getAllItems(org,r);
        return r;
    }

    private void getAllItems(ItemGroup<?> org, List<Item> r) {
        for (Item i : org.getItems()) {
            r.add(i);
            if (i instanceof ItemGroup) {
                getAllItems((ItemGroup)i,r);
            }
        }
    }

    /**
     * If the search restricts the scope to a specific org (aka ItemGroup), return that, or else
     * the default scope, which is {@link Jenkins}.
     */
    private ItemGroup org(Query q) {
        String org = q.param(ORGANIZATION_PARAM);
        if (org==null)  return Jenkins.getInstance();
        ItemGroup group = OrganizationFactory.getItemGroup(org);
        if (group==null) {
            throw new ServiceException.BadRequestException(
                String.format("Organization %s not found. Query parameter %s value: %s is invalid. ", org,ORGANIZATION_PARAM,org));
        }
        return group;
    }

    private boolean exclude(ItemGroup item, List<Class> excludeList){
        for(Class c:excludeList){
            if(c.isAssignableFrom(item.getClass())){
                return true;
            }
        }
        return false;
    }
}
