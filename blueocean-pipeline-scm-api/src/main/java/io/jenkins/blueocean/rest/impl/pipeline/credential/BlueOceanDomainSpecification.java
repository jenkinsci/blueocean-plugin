package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This is BlueOcean specific {@link DomainSpecification}.
 *
 * @author Vivek Pandey
 */
public class BlueOceanDomainSpecification extends DomainSpecification {
    public static final String DOMAIN_SPECIFICATION = "BlueOcean Credentials Domain Specification";

    @NonNull
    @Override
    public Result test(@NonNull DomainRequirement scope) {
        if(scope instanceof BlueOceanDomainRequirement){
            return Result.POSITIVE;
        }
        return Result.NEGATIVE;
    }
}
