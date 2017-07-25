package io.jenkins.blueocean.rest.factory.organization;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.ModifiableTopLevelItemGroup;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Maps BlueOcean organization and {@link ItemGroup}s.
 *
 * <p>
 * BlueOcean introduces a notion of "organization", which maps to {@link ItemGroup} in Jenkins in some
 * application-defined way; for example you can map the whole Jenkins into one organization (default),
 * or you can map organization to each of the top-level folder, ...
 *
 * <p>
 * For the rest of the BlueOcean code, the assumption is that some {@link ItemGroup} maps to an organization,
 * which means every Item in it belongs to this organization.
 *
 * <p>
 * This is a singleton extension point. All but the highest ordinal implementation is ignored.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class OrganizationFactory implements ExtensionPoint {
    /**
     * Looks up an organization by its name.
     *
     * @return null if the no such org exists.
     */
    public abstract BlueOrganization get(String name);

    /**
     * Iterates over all the organizations.
     */
    public abstract Collection<BlueOrganization> list();

    /**
     * If given group is an org, return its representation, or null.
     */
    public abstract BlueOrganization of(ItemGroup group);

    /**
     * Finds a nearest organization that contains the given {@link ItemGroup}.
     *
     * @return
     *      null if the given object doesn't belong to any organization.
     */
    @CheckForNull
    public BlueOrganization getContainingOrg(ItemGroup p) {
        while (true) {
            BlueOrganization n = of(p);
            if (n != null) {
                return n;
            }
            if (p instanceof Item) {
                p = ((Item) p).getParent();
            } else {
                return null; // hit the top
            }
        }
    }

    /**
     * Use {@link #getContainingOrg(Item)} instead.
     * @deprecated in 1.2
     * @param r run
     * @return organization
     */
    @CheckForNull
    @Deprecated
    public final BlueOrganization getContainingOrg(Run r) {
        return getContainingOrg(r.getParent());
    }

    @CheckForNull
    public final BlueOrganization getContainingOrg(Item i) {
        if (i instanceof ItemGroup) {
            return getContainingOrg((ItemGroup) i);
        } else {
            return getContainingOrg(i.getParent());
        }
    }

    @Nonnull
    public static OrganizationFactory getInstance() {
        OrganizationFactory r = ExtensionList.lookup(OrganizationFactory.class).get(0);
        if (r==null) {
            throw new AssertionError("No OrganizationFactory is installed");
        }
        return r;
    }

    @CheckForNull
    public static ModifiableTopLevelItemGroup getItemGroup(String org) {
        BlueOrganization blueOrganization = OrganizationFactory.getInstance().get(org);
        if (blueOrganization instanceof AbstractOrganization) {
            return ((AbstractOrganization)blueOrganization).getGroup();
        }
        return null;
    }
}
