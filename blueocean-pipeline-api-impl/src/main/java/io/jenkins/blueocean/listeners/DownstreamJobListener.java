package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.service.embedded.DownstreamJobAction;

import java.util.logging.Logger;

@Extension
public class DownstreamJobListener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(DownstreamJobListener.class.getName());

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {

//        synchronized (System.out) {
//            System.out.println("\n\n\n"); // TODO: RM
//            System.out.println("*********************************************"); // TODO: RM
//            System.out.println("  onStarted for run : " + run); // TODO: RM
//            System.out.println("           listener : " + listener); // TODO: RM
//            System.out.println("       CauseActions : "); // TODO: RM
//
//            for (CauseAction action : run.getActions(CauseAction.class)) {
//                System.out.println("                    *--| " + action); // TODO: RM
//                System.out.println("                       |  DisplayName : " + action.getDisplayName()); // TODO: RM
//                System.out.println("                       | IconFileName : " + action.getIconFileName()); // TODO: RM
//                System.out.println("                       |      UrlName : " + action.getUrlName()); // TODO: RM
//                System.out.println("                       | causes : "); // TODO: RM
//                for (Cause cause : action.getCauses()) {
//                    System.out.println("                                *--| " + cause); // TODO: RM
//                    System.out.println("                                   | class : " + cause.getClass()); // TODO: RM
//                    System.out.println("                                   |  desc : " + cause.getShortDescription()); // TODO: RM
//                }
//            }
//            System.out.println("*********************************************"); // TODO: RM
//            System.out.println("\n\n\n"); // TODO: RM
//        }

        for (CauseAction action : run.getActions(CauseAction.class)) {
            for (Cause cause : action.getCauses()) {
                 if (cause instanceof Cause.UpstreamCause) {
                     Run triggerRun = ((Cause.UpstreamCause) cause).getUpstreamRun();
                     String dbg = "***** Run " + run + " caused by upstream " + triggerRun + "\n";  // TODO: RM

                     Class cl = triggerRun.getClass();

                     while (cl != null) {
                         dbg += "        - " + cl.getName() + "\n";
                         cl = cl.getSuperclass();
                     }

                     dbg += " triggerRun.getResult : " + triggerRun.getResult() + "\n";
                     dbg += "triggerRun.isBuilding : " + triggerRun.isBuilding() + "\n";

                     dbg += "About to add the action...";
                     triggerRun.addAction(new DownstreamJobAction(run));
                     dbg += "Done!";

                     System.out.println("\n\n" + dbg + "\n\n");  // TODO: RM


                 }
            }
        }


//        DisplayURLProvider urls = DisplayURLProvider.get();
//        System.out.println("                   DisplayURLProvider.get() gave us " + DisplayURLProvider.get()); // TODO: RM
//        System.out.println("            DisplayURLProvider.getDefault() gave us " + DisplayURLProvider.getDefault()); // TODO: RM
//        System.out.println("  DisplayURLProvider.getPreferredProvider() gave us " + DisplayURLProvider.getPreferredProvider()); // TODO: RM
//
//
//
//        if (urls != null) {
//            for (BuildTriggerAction.Trigger trigger : BuildTriggerAction.triggersFor(run)) {
//                StepContext stepContext = trigger.context;
//                if (stepContext != null && stepContext.isReady()) {
//                    try {
//                        TaskListener taskListener = stepContext.get(TaskListener.class);
//                    taskListener.getLogger().println("Downstream build: " + run.getFullDisplayName() + " - " + urls.getRunURL(run));
//                    } catch (Exception e) {
//                        LOGGER.log(Level.WARNING, null, e);
//                    }
//                } else {
//                    LOGGER.log(Level.FINE, "{0} unavailable in {1}", new Object[]{stepContext, run});
//                }
//            }
//        }
    }

}
