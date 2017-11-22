package io.jenkins.blueocean.listeners;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.console.ModelHyperlinkNote;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.support.steps.build.BuildTriggerAction;

@Extension
public class DownstreamJobListener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(DownstreamJobListener.class.getName());

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {

        List<DisplayURLProvider> providers = ImmutableList.copyOf(DisplayURLProvider.all());

//        synchronized (System.out) {
//            System.out.println("\n\n\n"); // TODO: RM
//            System.out.println("*********************************************"); // TODO: RM
//            System.out.println("  onStarted for run : " + run); // TODO: RM
//            System.out.println("           listener : " + listener); // TODO: RM
//            System.out.println("        run actions : "); // TODO: RM
//
//            for (Action action : run.getActions()) {
//                System.out.println("                      * " + action); // TODO: RM
//            }
//
//
//            System.out.println("DisplayURLProviders : " + providers.size()); // TODO: RM
//
//            for (DisplayURLProvider p : providers) {
//                System.out.println("                    * " + p); // TODO: RM
//            }
//
//
//            System.out.println("*********************************************"); // TODO: RM
//            System.out.println("\n\n\n"); // TODO: RM
//        }


        DisplayURLProvider urls = DisplayURLProvider.get();
        System.out.println("                   DisplayURLProvider.get() gave us " + DisplayURLProvider.get()); // TODO: RM
        System.out.println("            DisplayURLProvider.getDefault() gave us " + DisplayURLProvider.getDefault()); // TODO: RM
        System.out.println("  DisplayURLProvider.getPreferredProvider() gave us " + DisplayURLProvider.getPreferredProvider()); // TODO: RM



        if (urls != null) {
            for (BuildTriggerAction.Trigger trigger : BuildTriggerAction.triggersFor(run)) {
                StepContext stepContext = trigger.context;
                if (stepContext != null && stepContext.isReady()) {
                    try {
                        TaskListener taskListener = stepContext.get(TaskListener.class);
                    taskListener.getLogger().println("Downstream build: " + run.getFullDisplayName() + " - " + urls.getRunURL(run));
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, null, e);
                    }
                } else {
                    LOGGER.log(Level.FINE, "{0} unavailable in {1}", new Object[]{stepContext, run});
                }
            }
        }
    }

}
