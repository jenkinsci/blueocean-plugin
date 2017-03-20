package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.GENERIC_RESOURCE;

/**
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
@Capability(GENERIC_RESOURCE)
public class GenericResource<T> extends Resource {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericResource.class);

    private final T value;
    private final Reachable self;

    public GenericResource(T value) {
        this.value = value;
        this.self = null;
    }

    public GenericResource(T value, Reachable self) {
        this.value = value;
        this.self = self;
    }

    @Exported(merge = false)
    @Override
    public Object getState() {
        return value;
    }

    /**
     * Allows value to expose additional routes
     *
     * @param token route to look for in this value
     * @return Object corresponding to given token
     * @throws io.jenkins.blueocean.commons.ServiceException.NotFoundException if no route found
     */
    public Object getDynamic(String token) {
        Class clz = value.getClass();
        try {
            // TODO: this only take care of getXyz() style methods. It needs to take care of other types of url
            // path handling
            Method m = clz.getMethod("get"+ Character.toUpperCase(token.charAt(0))+token.substring(1));
            return getResource(token, m);
        } catch (NoSuchMethodException e) {
            for(Method m:clz.getMethods()){
                Exported exported = m.getAnnotation(Exported.class);
                if(exported != null && exported.name().equals(token)){
                    return getResource(token, m);
                }
            }
            return null;
        }
    }

    @Override
    public Link getLink() {
        return (self !=null) ? self.getLink() : new Link(Stapler.getCurrentRequest().getPathInfo());
    }

    private Object getResource(String token, Method m){
        final Object v;
        try {
            v = m.invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException.NotFoundException("Path "+token+" is not found");
        }
        Link subResLink = getLink().rel(token+"/");
        if(v instanceof List){
            return Containers.from(subResLink,(List)v);
        }else if(v instanceof Map){
            return Containers.from(subResLink,(Map)v);
        }else if(v instanceof String){
            return new PrimitiveTypeResource(subResLink,v);
        }
        return new GenericResource<>(v);
    }


    /**
     * Resource that exposes primitive type value as JSON bean
     */
    public static class PrimitiveTypeResource extends Resource {
        private final Link self;
        private final Object v;

        public PrimitiveTypeResource(Link self, Object v) {
            this.self = self;
            this.v = v;
        }

        @Override
        public Link getLink() {
            return self;
        }

        @Exported
        public Object getValue(){
            return v;
        }
    }
}
