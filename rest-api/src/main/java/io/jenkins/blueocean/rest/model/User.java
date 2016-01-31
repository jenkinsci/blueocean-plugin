package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.guice.JsonConverter;
import io.jenkins.blueocean.rest.model.hal.Link;
import io.jenkins.blueocean.rest.model.hal.Links;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User model.
 *
 * @author Vivek Pandey
 **/
public class User {
    public final String id;
    public final String email;
    public final String name;
    public final UserLinks links;

    /**
     *  User constructor
     *
     * @param id user id to uniquely identify a user
     * @param email user's email
     * @param name user's name
     * @param link user's links referenes
     */
    public User(@Nonnull @JsonProperty("id") String id,
                @Nonnull @JsonProperty("email") String email,
                @Nullable @JsonProperty("name") String name,
                @Nullable @JsonProperty("_links") UserLinks link) {
        this.email = email;
        this.name = name;
        this.id = id;
        this.links = link;
    }

    public static class UserLinks extends Links {

        public final Link organization;

        public final Link pipeline;

        public UserLinks(@Nonnull @JsonProperty("self")Link self,
                         @Nonnull @JsonProperty("organization") Link organization,
                         @Nonnull @JsonProperty("pipeline") Link pipeline) {
            super(self);
            this.organization = organization;
            this.pipeline = pipeline;
        }
    }

    public static class Builder {
        private String id, name, email;
        private UserLinks links;

        public Builder(String id) {
            this.id = id;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder links(UserLinks links) {
            this.links = links;
            return this;
        }

        public User build() {
            return new User(id, email, name, links);
        }
    }

    public static void main(String[] args) {
        System.out.println(JsonConverter.fromJava(new Builder("123")
                .email("abc@def.com").name("abc")
                .links(new UserLinks(new Link("/users/123"),
                        new Link("/users/123/organizations"),
                        new Link("/users/123/jobs"))).build()));
    }
}
