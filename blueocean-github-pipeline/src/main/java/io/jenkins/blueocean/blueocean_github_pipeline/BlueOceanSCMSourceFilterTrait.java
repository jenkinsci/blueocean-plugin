package io.jenkins.blueocean.blueocean_github_pipeline;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourcePrefilter;
import jenkins.scm.impl.trait.Selection;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

public class BlueOceanSCMSourceFilterTrait extends SCMNavigatorTrait {

    private final Set<String> names;


    public BlueOceanSCMSourceFilterTrait(String namesStr) {
        names = new TreeSet<>();
        for (String name: StringUtils.split(namesStr, "\n")) {
            name = StringUtils.trim(name);
            if (!StringUtils.isBlank(name)) {
                names.add(name);
            }
        }
    }

    public BlueOceanSCMSourceFilterTrait(Collection<String> names) {
        this.names = new TreeSet<>(names);
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    public String getNamesStr() {
        return StringUtils.join(names, "\n");
    }

    @Override
    protected void decorateContext(SCMNavigatorContext<?, ?> context) {
        context.withPrefilter(new Prefilter(names));
    }

    public static class Prefilter extends SCMSourcePrefilter{

        private final Set<String> names;

        public Prefilter(Set<String> names) {
            this.names = names;
        }

        @Override
        public boolean isExcluded(@NonNull SCMNavigator source, @NonNull String projectName) {
            if (names.contains(projectName)) {
                return false;
            }
            // GitHub is case insensitive for repository names, but can return different case under different code paths
            for (String name: names) {
                if (name.equalsIgnoreCase(projectName)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Extension
    @Selection
    public static class DescriptorImpl extends SCMNavigatorTraitDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Only specified projects";
        }

        @Override
        public Class<? extends SCMNavigator> getNavigatorClass() {
            return GitHubSCMNavigator.class;
        }
    }
}
