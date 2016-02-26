package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class UserSearch extends OmniSearch<BlueUser> {
    @Override
    public String getType() {
        return "user";
    }

    @Override
    public Pageable<BlueUser> search(Query q) {
        List<BlueUser> users = new ArrayList<>();
        for(hudson.model.User u:hudson.model.User.getAll()){
            users.add(new UserImpl(u));
        }
        return Pageables.wrap(users);
    }
}
