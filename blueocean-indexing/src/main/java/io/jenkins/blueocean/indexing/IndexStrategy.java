package io.jenkins.blueocean.indexing;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

public abstract class IndexStrategy implements ExtensionPoint {

    public static Item find(Run<?, ?> run) {
        ExtensionList<IndexStrategy> extensionList = Jenkins.getInstance().getExtensionList(IndexStrategy.class);
        for (IndexStrategy strategy : extensionList) {
            Item item = strategy.findIndexParent(run);
            if (item != null) {
                return item;
            }
        }
        return run.getParent();
    }

    public abstract Item findIndexParent(Run<?, ?> run);
}
