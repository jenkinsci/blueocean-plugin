package io.jenkins.blueocean.rest.router;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.HttpMethod;
import io.jenkins.blueocean.rest.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public  class Router {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RouteMatcher.class);
    private static final char SINGLE_QUOTE = '\'';

    private final RouteMatcher routeMatcher = new RouteMatcher();

    private final List<RouteEntry> routes = new ArrayList<>();

    private Router() {
    }

    public static final Router INSTANCE = new Router();

    /**
     * Adds a route
     *
     * @param httpMethod the HTTP method
     * @param route      the route implementation
     */
    public  void addRoute(HttpMethod httpMethod, Route route){
       routeMatcher.parseValidateAddRoute(httpMethod, route);
    }

    /**
     * Adds a filter
     *
     * @param httpMethod the HTTP method
     * @param filter     the route implementation
     */
    public  void addFilter(HttpMethod httpMethod, Route filter){
        routeMatcher.parseValidateAddRoute(httpMethod, filter);
    }


    public static void get(Route route){
        INSTANCE.addRoute(HttpMethod.get, route);
    }



    public static void post(Route route){
        INSTANCE.addRoute(HttpMethod.post, route);
    }


    public static void put(Route route){
        INSTANCE.addRoute(HttpMethod.put, route);
    }

    public static void patch(Route route){
        INSTANCE.addRoute(HttpMethod.patch, route);
    }

    public static void head(Route route){
        INSTANCE.addRoute(HttpMethod.head, route);
    }

    public RouteMatcher routeMatcher(){
        return routeMatcher;
    }

    public static void execute(RouteContext context){
        Object content=null;

        RouteMatch match = context.routeMatcher().findTargetForRequestedRoute(context.httpMethod(), context.uri(), context.acceptType());

        Route target = null;
        if (match != null) {
            target = match.getTarget();
        } else if (context.httpMethod() == HttpMethod.head && context.body().notSet()) {
            // See if get is mapped to provide default head mapping
            match = context.routeMatcher().findTargetForRequestedRoute(HttpMethod.get, context.uri(), context.acceptType());
            content = match != null ? "" : null;
        }


        if (target == null) {
            throw new ServiceException.NotFoundException(
                String.format("No route found for requested resource %s with HTTP method %s",
                    context.uri(), context.httpMethod()));
        }

        Request request  = new Request(match, context.httpRequest());
        Object result = target.handle(request, context.response());;

        if (result != null) {
            content = result;
        }
        context.body().set(content);
    }
}
