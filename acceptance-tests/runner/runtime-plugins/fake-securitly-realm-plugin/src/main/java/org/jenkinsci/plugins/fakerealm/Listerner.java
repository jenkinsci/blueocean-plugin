package org.jenkinsci.plugins.fakerealm;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.slaves.ComputerListener;
import jenkins.model.Jenkins;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.jvnet.hudson.test.JenkinsRule;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.util.*;

@Extension
public class Listerner  extends ComputerListener{
    boolean set = false;
    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if(!set) {
            Jenkins.getActiveInstance().setSecurityRealm(new DummySecurityRealm());
            set = true;
        }

    }

    public static class DummySecurityRealm extends AbstractPasswordBasedSecurityRealm {

        private final Map<String,Set<String>> groupsByUser = new HashMap<String,Set<String>>();

        DummySecurityRealm() {}

        @Override
        protected UserDetails authenticate(String username, String password) throws AuthenticationException {
            if (username.equals(password))
                return loadUserByUsername(username);
            throw new BadCredentialsException(username);
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
                DataAccessException {
            List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
            auths.add(AUTHENTICATED_AUTHORITY);
            Set<String> groups = groupsByUser.get(username);
            if (groups != null) {
                for (String g : groups) {
                    auths.add(new GrantedAuthorityImpl(g));
                }
            }
            return new org.acegisecurity.userdetails.User(username,"",true,true,true,true, auths.toArray(new GrantedAuthority[auths.size()]));
        }

        @Override
        public GroupDetails loadGroupByGroupname(final String groupname) throws UsernameNotFoundException, DataAccessException {
            for (Set<String> groups : groupsByUser.values()) {
                if (groups.contains(groupname)) {
                    return new GroupDetails() {
                        @Override
                        public String getName() {
                            return groupname;
                        }
                    };
                }
            }
            throw new UsernameNotFoundException(groupname);
        }

        /** Associate some groups with a username. */
        public void addGroups(String username, String... groups) {
            Set<String> gs = groupsByUser.get(username);
            if (gs == null) {
                groupsByUser.put(username, gs = new TreeSet<String>());
            }
            gs.addAll(Arrays.asList(groups));
        }

    }

}
