package io.jenkins.blueocean.rest;

import com.google.common.collect.ImmutableSet;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CapabilitiesTest {

    @Test
    public void testAllCapabilities() {
        assertEquals(ImmutableSet.of(), Capabilities.allCapabilities(Object.class));
        assertEquals(ImmutableSet.of(), Capabilities.allCapabilities(A.class));
        assertEquals(ImmutableSet.of("b"), Capabilities.allCapabilities(B.class));
        assertEquals(ImmutableSet.of("b"), Capabilities.allCapabilities(C.class));
        assertEquals(ImmutableSet.of("d", "b"), Capabilities.allCapabilities(D.class));
    }

    @Test
    public void testAllSuperclassesAndCapabilities() {
        assertEquals(ImmutableSet.of(), Capabilities.allSuperClassesAndCapabilities(Object.class));
        assertEquals(ImmutableSet.of(), Capabilities.allSuperClassesAndCapabilities(A.class));
        assertEquals(ImmutableSet.of("b", "io.jenkins.blueocean.rest.CapabilitiesTest$A"), Capabilities.allSuperClassesAndCapabilities(B.class));
        assertEquals(ImmutableSet.of("b", "io.jenkins.blueocean.rest.CapabilitiesTest$B", "io.jenkins.blueocean.rest.CapabilitiesTest$A"), Capabilities.allSuperClassesAndCapabilities(C.class));
        assertEquals(ImmutableSet.of("d", "io.jenkins.blueocean.rest.CapabilitiesTest$C", "b", "io.jenkins.blueocean.rest.CapabilitiesTest$B", "io.jenkins.blueocean.rest.CapabilitiesTest$A"), Capabilities.allSuperClassesAndCapabilities(D.class));
    }

    class A {}

    @Capability("b")
    class B extends A {}

    class C extends B {}

    @Capability("d")
    class D extends C {}
}
