package io.jenkins.blueocean.service.embedded.util;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;

public class OrganizationUtil {

    private static final Pattern pattern = Pattern.compile("/blue/organizations/([^/]*)/");

    /**
     * Gets the organization name from the URL in the form of:
     * 
     * <pre>
     * /blue/organizations/ORG_NAME/...
     * </pre>
     * 
     * @return the organization name if found, {code}null{code} if not
     */
    @CheckForNull
    public static String getOrganizationNameFromURL() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (currentRequest == null) {
            return null;
        }

        String requestURI = currentRequest.getRequestURI();

        if (requestURI == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(requestURI);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Returns the organization for a given name, defaulting to the first organization if not found or if the
     * organization name parameter was null
     * 
     * @param orgName name of the organization to lookup
     * @param defaultToFirst if true the result will default to the first organization when the name comes as null or no
     * organization with that name could be found. When false it will return null if the organization couldn't be found
     * @return the organization or null if it couldn't be found.
     */
    @CheckForNull
    public static BlueOrganization getOrganization(@CheckForNull String orgName, boolean defaultToFirst) {

        OrganizationFactory orgFactory = OrganizationFactory.getInstance();
        BlueOrganization organization = null;

        if (orgName != null) {
            organization = orgFactory.get(orgName);
        }

        if (organization == null) {
            Iterator<BlueOrganization> iterator = orgFactory.list().iterator();
            if (iterator.hasNext()) {
                organization = iterator.next();
            }
        }
        return organization;
    }

    /**
     * Returns the first organization available
     * 
     * @return the first organization
     */
    public static BlueOrganization getFirst() {
        return OrganizationFactory.getInstance().list().iterator().next();
    }

}
