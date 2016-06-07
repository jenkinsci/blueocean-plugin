package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class GenericResource<T> extends Resource {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericResource.class);

    private final T value;

    public GenericResource(T value) {
        this.value = value;
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

            final Object v = m.invoke(value);
            if(v instanceof List){
                return Containers.from((List)v);
            }else if(v instanceof Map){
                return Containers.from((Map)v);
            }else if(v instanceof String){
                return new PrimitiveTypeResource(v);
            }
            return new GenericResource<>(v);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException.NotFoundException("Path "+token+" is not found");
        }
    }


    /**
     * Resource that exposes primitive type value as JSON bean
     */
    public static class PrimitiveTypeResource extends Resource{
        private final Object v;

        public PrimitiveTypeResource(Object v) {
            this.v = v;
        }

        @Exported
        public Object getValue(){
            return v;
        }
    }
}
