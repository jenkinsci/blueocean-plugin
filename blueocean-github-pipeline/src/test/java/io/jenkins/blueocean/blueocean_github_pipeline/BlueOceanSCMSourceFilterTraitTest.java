package io.jenkins.blueocean.blueocean_github_pipeline;

import jenkins.scm.api.SCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigatorContext;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class BlueOceanSCMSourceFilterTraitTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given__trait__when__decoratingContext__then__prefilterApplied() throws Exception {
        BlueOceanSCMSourceFilterTrait instance = new BlueOceanSCMSourceFilterTrait("foo\\nmanchu");
        GitHubSCMNavigatorContext probe = new GitHubSCMNavigatorContext();
        instance.decorateContext(probe);
        assertThat(probe.prefilters(),contains(instanceOf(BlueOceanSCMSourceFilterTrait.Prefilter.class)));
        BlueOceanSCMSourceFilterTrait.Prefilter prefilter =
            (BlueOceanSCMSourceFilterTrait.Prefilter) probe.prefilters().get(0);
        assertThat(prefilter.isExcluded(new GitHubSCMNavigator("dummy"), "foo"), is(false));
        assertThat(prefilter.isExcluded(new GitHubSCMNavigator("dummy"), "fu"), is(true));
        assertThat(prefilter.isExcluded(new GitHubSCMNavigator("dummy"), "manchu"), is(false));
        assertThat(prefilter.isExcluded(new GitHubSCMNavigator("dummy"), "manChu"), is(false));
        assertThat(prefilter.isExcluded(new GitHubSCMNavigator("dummy"), "bar"), is(true));
    }

    @Test
    public void given__trait__when__testingApplicability__then__limitedToGitHub() throws Exception {
        BlueOceanSCMSourceFilterTrait instance = new BlueOceanSCMSourceFilterTrait("foo\\nmanchu");
        assertThat(instance.getDescriptor().isApplicableTo(GitHubSCMNavigator.class), is(true));
        assertThat(instance.getDescriptor().isApplicableTo(SCMNavigator.class), is(false));
    }


}
