package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Vivek Pandey
 */
public class ActionProxiesImpl extends BlueActionProxy {
    private final Action action;
    private final Reachable parent;
    private static final Logger logger = LoggerFactory.getLogger(ActionProxiesImpl.class);

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
        if (action instanceof Reachable) {
            return ((Reachable) action).getLink();
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
        if(isTreeRequest()){
            return getActionProxies(actions, action -> false, parent );
        }
        return Collections.emptyList();

    }

    /**
     * Finds all the actions and proxys them so long as they are annotated with ExportedBean or match the provided predicate
     * @param actions to proxy
     * @param alwaysAllowAction predicate to positively filter
     * @param parent reachable
     * @return actionProxies
     */
    public static Collection<BlueActionProxy> getActionProxies(Collection<? extends Action> actions, final Predicate<Action> alwaysAllowAction, Reachable parent){
        return actions.stream().filter( (Predicate<Action>) action -> action != null
            && (action.getClass().getAnnotation(ExportedBean.class) != null || alwaysAllowAction.test(action))).
            map(action1 -> new ActionProxiesImpl(action1, parent)).
            collect(Collectors.toList());

    }

    // Should be called only in request context
    private static boolean isTreeRequest(){
        return StringUtils.isNotBlank(Stapler.getCurrentRequest().getParameter("tree"));
    }
}
