package io.jenkins.blueocean.service.embedded;

import com.cloudbees.hudson.plugins.folder.computed.ComputedFolder;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.blueocean.BlueOceanUIProvider;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.model.ModifiableTopLevelItemGroup;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public class BlueOceanUrlMapperImpl extends BlueOceanUrlMapper {
    @Override
    public String getUrl(@Nonnull ModelObject modelObject) {
        BlueOrganization organization = getOrganization(modelObject);
        if(organization == null){ //no organization, best we can do is to land user on landing page
            return getLandingPagePath();
        }
        String organizationName = organization.getName();
        String baseUrl = getOrgPrefix(organizationName);
        if (modelObject instanceof ModifiableTopLevelItemGroup){
            return baseUrl;
        } else if (modelObject instanceof Job) {
            BluePipeline blueResource = getJobResource(modelObject);
            if(blueResource != null){
                return getPipelineUrl(baseUrl, blueResource);
            }
        } else if (modelObject instanceof Run) {
            Run run = (Run) modelObject;
            Job job = run.getParent();
            BluePipeline blueResource = getJobResource(job);
            if(blueResource != null){
                // The job can be created with a name that has special encoding chars in it (if created outside the UI e.g. MBP indexing),
                // specifically %. Encoding it again breaks things ala JENKINS-40137. The creation name can also
                // have spaces, even from the UI (it should prevent that). So, decode to revert anything that's already
                // encoded and re-encode to do the full monty. Nasty :)
                return baseUrl + "/" + encodeURIComponent(blueResource.getFullName()) + "/detail/" + encodeURIComponent(decodeURIComponent(job.getName())) + "/" + encodeURIComponent(run.getId()) + "/";
            }
        } else if(modelObject instanceof Item){
            Resource bluePipeline = BluePipelineFactory.resolve((Item)modelObject);
            if(bluePipeline instanceof BlueMultiBranchPipeline){
                return getPipelineUrl(baseUrl, (BluePipeline) bluePipeline);
            }
        }
        return null;
    }

    private String getPipelineUrl(String baseUrl, BluePipeline pipeline){
        if(pipeline instanceof BlueMultiBranchPipeline){
            return baseUrl + "/" + encodeURIComponent(pipeline.getFullName()) + "/branches/";
        }else{
            return baseUrl + "/" + encodeURIComponent(pipeline.getFullName()) + "/";
        }
    }

    private @CheckForNull BluePipeline getJobResource(ModelObject modelObject){
        BluePipeline blueResource = null;
        if(modelObject instanceof Job){
            ItemGroup parent = ((Job) modelObject).getParent();
            if(parent instanceof ComputedFolder){
                blueResource =  (BluePipeline) BluePipelineFactory
                        .resolve((ComputedFolder)parent);
                if(blueResource instanceof BlueMultiBranchPipeline){
                    return blueResource;
                }
            }else {
                blueResource =  (BluePipeline) BluePipelineFactory
                        .resolve((Job)modelObject);
            }
        }
        return blueResource;
    }

    private boolean isBranch(ModelObject modelObject){
        if(modelObject instanceof Job){
            ItemGroup parent = ((Job) modelObject).getParent();
            if(parent instanceof ComputedFolder){
                BluePipeline blueResource = (BluePipeline) BluePipelineFactory
                        .resolve((Job)modelObject);
                return (blueResource instanceof BlueMultiBranchPipeline);
            }
        }
        return false;
    }

    static String getLandingPagePath(){
        for(BlueOceanUIProvider p:BlueOceanUIProvider.all()){
            return String.format("%s%s",getBlueHome(), p.getLandingPagePath());
        }
        return getBlueHome();
    }

    private static String getBlueHome() {
        for(BlueOceanUIProvider p:BlueOceanUIProvider.all()){
            return p.getUrlBasePrefix();
        }
        return "blue"; //fallback, but this statement will not get executed as there is a default provider
    }

    private String decodeURIComponent(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected UTF-8 encoding error.", e);
        }
    }

    private String encodeURIComponent(String string) {
        try {
            // The Java URLEncoder encodes spaces as "+", while the javascript
            // encodeURIComponent function encodes them as "%20". We need to make them
            // consistent with how it's done in encodeURIComponent, so replace the
            // "+" with "%20".
            return URLEncoder.encode(string, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected UTF-8 encoding error.", e);
        }
    }

    private BlueOrganization getOrganization(ModelObject modelObject ){
        BlueOrganization organization = null;
        if(modelObject instanceof Item){
            organization = OrganizationFactory.getInstance().getContainingOrg((Item) modelObject);
        }else if(modelObject instanceof ItemGroup){
            organization = OrganizationFactory.getInstance().getContainingOrg((ItemGroup) modelObject);
        }else if(modelObject instanceof Run){
            organization = OrganizationFactory.getInstance().getContainingOrg(((Run) modelObject).getParent());
        }
        return organization;
    }

    private String getOrgPrefix(String organization){
        return String.format("%s/organizations/%s", getBlueHome(), organization);
    }
}
