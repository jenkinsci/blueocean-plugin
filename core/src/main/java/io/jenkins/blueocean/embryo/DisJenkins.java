package io.jenkins.blueocean.embryo;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Descriptor.FormException;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewGroup;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.jenkinsci.plugins.variant.VariantSet;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Take over the root UI object from Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
public class DisJenkins extends View implements StaplerProxy {
    @Inject
    private transient App app;

    public DisJenkins(String name, ViewGroup owner) {
        super(name, owner);
    }


    @Override
    public Object getTarget() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins instance is not ready");
        }
        return jenkins.getExtensionList(App.class).get(0);
    }

/*
 * Stub out the View methods.
 */
    @Override
    public boolean contains(TopLevelItem item) {
        return false;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return Collections.emptyList();
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * The way we take over the UI is by taking over the root view.
     *
     * @param j The Jenkins instance
     * @throws IOException in case something goes wrong when creating views or when saving Jenkins configuration
     */
    @Initializer(after= InitMilestone.JOB_LOADED, before= InitMilestone.COMPLETED)
    public static void install(Jenkins j) throws IOException {
        LOGGER.log(Level.INFO, "Configuring Embryo Root View");
        DisJenkins v = new DisJenkins("Root", j);
        j.addView(v);
        j.setPrimaryView(v);
        boolean seenDisJenkins = false;
        for (View oldView : j.getViews()) {
            if (seenDisJenkins) {
                j.deleteView(oldView);
            } else {
                seenDisJenkins |= oldView instanceof DisJenkins;
                if (!(oldView instanceof DisJenkins)) {
                    j.deleteView(oldView);
                }
            }
        }
        j.save();
    }

    /**
     * I see some cases where Embryo comes up without DisJenkins installed,
     * so doubly make sure we have it installed.
     */
    @Extension
    public static class ItemListenerImpl extends ItemListener {
        @Inject
        private Jenkins jenkins;

        @Override
        public void onLoaded() {
            try {
                install(jenkins);
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Failed to install Embryo");
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DisJenkins.class.getName());
}
