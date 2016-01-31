package io.jenkins.blueocean.rest.model.hal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.guice.JsonConverter;

/**
 * Implementation of
 * <a href="https://tools.ietf.org/html/draft-kelly-json-hal-07">JSON Hipertext application language (HAL)</a>.
 *
 * <ul>
 *     <li>Only -links element is implemented</li>
 *     <li>Only required href element is supported</li>
 *     <li>TODO: decide on whether application/hal+json or just use application/json</li>
 * </ul>
 *
 * Any implementation should pass the self reference when constructing this object. Implementing class should define
 * it's own Link reference.
 *
 * Example:
 * <pre>
 * <code>
 *   public class OrganizationLink extends Link{
 *       public OrganizationLink(
 *   }
 * </code>
 * </pre>
 *
 * @author Vivek Pandey
 **/
public abstract class Links {
    public final Link self;

    public Links(@JsonProperty("self") Link self) {
        this.self = self;
    }

    public static void main(String[] args) {
        System.out.println(JsonConverter.fromJava(new Links(new Link("/users/23")) {
            public Link organization = new Link("/organizations/12");
        }));
    }

    public static class OrganizationLink extends Links{
        public final Link organization;
        public OrganizationLink(Link self, Link orgLink) {
            super(self);
            this.organization = orgLink;
        }


    }

    public static class Organization{
        public Links _links;

        public static void main(String[] args) {
            Organization organization = new Organization();
            organization._links = new OrganizationLink(new Link("/users/23"), new Link("/organizations/12"));
            System.out.println(JsonConverter.fromJava(organization));
        }
    }
}


