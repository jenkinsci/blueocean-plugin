package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Predicate;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.github.GHRepository;

import javax.annotation.Nullable;

@Restricted(NoExternalUse.class)
class GithubPredicates {
    static Predicate<GHRepository> hasAdminAccess() {
        return new Predicate<GHRepository>() {
            @Override
            public boolean apply(@Nullable GHRepository input) {
                return input != null && input.hasAdminAccess();
            }
        };
    }

    static Predicate<GHRepository> hasPushAccess() {
        return new Predicate<GHRepository>() {
            @Override
            public boolean apply(@Nullable GHRepository input) {
                return input != null && input.hasPushAccess();
            }
        };
    }

    private GithubPredicates() {}
}
