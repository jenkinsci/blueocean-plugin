package io.jenkins.blueocean.commons.stapler.export;

import com.google.common.base.Predicate;

/**
 * Decorates a base {@link TreePruner} by refusing additional properties
 * instructed by {@linkplain Predicate an external logic.}
 *
 * @author Kohsuke Kawaguchi
 */
class FilteringTreePruner extends TreePruner {
    private final Predicate<String> predicate;
    private final TreePruner base;

    FilteringTreePruner(Predicate<String> predicate, TreePruner base) {
        if (predicate == null) throw new IllegalArgumentException();
        if (base == null) throw new IllegalArgumentException();

        this.predicate = predicate;
        this.base = base;
    }

    @Override
    public TreePruner accept(Object node, Property prop) {
        if (predicate.apply(prop.name))
            return null;
        TreePruner child = base.accept(node, prop);

        // for merge properties, the current restrictions on the property names should
        // still apply to the child TreePruner
        if (prop.merge && child != null)
            child = new FilteringTreePruner(predicate,child);

        return child;
    }
}
