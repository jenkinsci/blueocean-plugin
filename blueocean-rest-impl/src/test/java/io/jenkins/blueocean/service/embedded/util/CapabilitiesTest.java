package io.jenkins.blueocean.service.embedded.util;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.search.Search;
import hudson.search.SearchIndex;
import hudson.security.ACL;
import hudson.security.Permission;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.acegisecurity.AccessDeniedException;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

public class CapabilitiesTest {

    @Test
    public void shouldDetectAnnotationsOnBase() {
        AlphaClass base = new AlphaClass();

        assertTrue("should find alpha cap on AlphaClass", Capabilities.hasCapability(base, "alpha"));
        assertFalse("should not find bravo cap on AlphaClass", Capabilities.hasCapability(base, "bravo"));
        assertFalse("should not find blahblah cap on AlphaClass", Capabilities.hasCapability(base, "blahblah"));
    }

    @Test
    public void shouldDetectAnnotationsOnDerivedClasses() {
        BravoClass bravo = new BravoClass();
        CharlieClass charlie = new CharlieClass();

        assertTrue("should find bravo cap on BravoClass", Capabilities.hasCapability(bravo, "bravo"));
        assertFalse("should not find blahblah cap on BravoClass", Capabilities.hasCapability(bravo, "blahblah"));

        assertTrue("should find charlie cap on CharlieClass", Capabilities.hasCapability(charlie, "charlie"));
        assertFalse("should not find blahblah cap on CharlieClass", Capabilities.hasCapability(charlie, "blahblah"));
    }

    @Test
    public void shouldInheritCapabilities() {
        BravoClass bravo = new BravoClass();
        CharlieClass charlie = new CharlieClass();

        assertTrue("should find alpha cap on BravoClass", Capabilities.hasCapability(bravo, "alpha"));
        assertTrue("should find alpha cap on CharlieClass", Capabilities.hasCapability(charlie, "alpha"));
    }

    @Test
    public void shouldNotFailWhenAnnotationMissing() {
        Capabilities.hasCapability(new BaseClass(), "anything");
    }

    // Just some useless classes to test with
    @Capability("alpha")
    private static class AlphaClass extends BaseClass {}

    @Capability("bravo")
    private static class BravoClass extends AlphaClass {}

    @Capability("charlie")
    private static class CharlieClass extends AlphaClass {}

    private static class BaseClass {}
}
