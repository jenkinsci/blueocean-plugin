package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import hudson.model.Action;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class ActionProxiesImpl extends BlueActionProxy {
    private final Action action;
    private final Reachable parent;
    private static final Logger logger = LoggerFactory.getLogger(ActionProxiesImpl.class);
    private static final ImmutableSet<String> BANNED_ACTIONS = ImmutableSet.of("org.jenkinsci.plugins.workflow.job.views.FlowGraphAction", "hudson.plugins.jobConfigHistory.JobConfigHistoryProjectAction");

    public ActionProxiesImpl(Action action, Reachable parent) {
        this.action = action;
        this.parent = parent;
    }


    @Override
    public Object getAction() {
        if(action.getClass().isAnnotationPresent(ExportedBean.class)){
            return action;
        }else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        try {
            return action.getUrlName();
        }catch (Exception e){
            logger.error(String.format("Error calling %s.getUrlName(): %s", action.getClass().getName(), e.getMessage()),e);
            return null;
        }
    }

    @Override
    public String get_Class() {
        return action.getClass().getName();
    }

    @Override
    public Link getLink() {
        if(getUrlName() != null) {
            return parent.getLink().rel(getUrlName());
        }
        return null;
    }

    /**
     * Finds all the actions and proxys them so long as they are annotated with ExportedBean
     * @param actions to proxy
     * @param parent reachable
     * @return actionProxies
     */
    public static Collection<BlueActionProxy> getActionProxies(List<? extends Action> actions, Reachable parent){
        return getActionProxies(actions, Predicates.<Action>alwaysFalse(), parent);
    }

    /**
     * Finds all the actions and proxys them so long as they are annotated with ExportedBean or match the provided predicate
     * @param actions to proxy
     * @param alwaysAllowAction predicate to positively filter
     * @param parent reachable
     * @return actionProxies
     */
    public static Collection<BlueActionProxy> getActionProxies(List<? extends Action> actions, final Predicate<Action> alwaysAllowAction, Reachable parent){
        List<BlueActionProxy> actionProxies = new ArrayList<>();
        for(Action action : Iterables.filter(actions, new Predicate<Action>() {
            @Override
            public boolean apply(Action action) {
                return action != null
                    && (action.getClass().getAnnotation(ExportedBean.class) != null || alwaysAllowAction.apply(action))
                    && !BANNED_ACTIONS.contains(action.getClass().getName());
            }
        })){
            actionProxies.add(new ActionProxiesImpl(action, parent));
        }
        return actionProxies;

    }
}
