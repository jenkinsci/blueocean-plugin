package io.jenkins.blueocean.rest.hal;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.model.Container;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.ExportedBean;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Implementation of
 * <a href="https://tools.ietf.org/html/draft-kelly-json-hal-07">JSON Hypertext application language (HAL)</a>.
 *
 * <ul>
 *     <li>Only _links element is implemented</li>
 *     <li>Only required href element is supported</li>
 *     <li>TODO: decide on whether application/hal+json or just use application/json</li>
 * </ul>
 *
 * Any {@link io.jenkins.blueocean.rest.model.Resource} implementation can add their resource specific path by calling
 * {@link Links#add(String, Link)}
 *
 * Example:
 * <pre>
 * <code>
 *
 *   {
 *       "_links":{
 *          "self":{"href":"http://.../pipelines/1/runs/23"},
 *          "test":{"href":"http://.../pipelines/1/runs/23/test"}
 *       }
 *   }
 * </code>
 * </pre>
 *
 * @author Vivek Pandey
 * @see Link
 * @see io.jenkins.blueocean.rest.model.Resource
 **/
@ExportedBean
public final class Links extends HashMap<String,Link>{

    private static final String SELF = "self";
    public Links() {
        getOrCreateSelfRef();
        for(Ancestor ancestor:Stapler.getCurrentRequest().getAncestors()){
            populateReferences(ancestor);
        }

    }

    public Links add(String ref, Link link){
        put(ref, link);
        return this;
    }

    private void populateReferences(Ancestor ancestor){
        Class clazz = ancestor.getObject().getClass();
        /** Find if there is method returning a {@link Container}, add this as link */
        Method m = findMethod(clazz,clazz);
        if(m!=null){
            String p = getPathFromMethodName(m.getName());
            put(p, createLinkRef(p));
        }
    }

    private Method  findMethod(Class c, Type logical){
        Method m;
        for (Class i : c.getInterfaces()) {
            m=findMethod(i, Types.getBaseClass(logical,i));
            if(m!=null){
                return m;
            }
        }

        Class sc = c.getSuperclass();
        if (sc!=null) {
            m = findMethod(sc, Types.getBaseClass(logical, sc));
            if(m != null){
                return m;
            }
        }

        for (Method method : c.getDeclaredMethods()) {
            if(method.getAnnotation(Navigable.class) != null){
                return method;
            }
        }
        return null;
    }

    private Link getOrCreateSelfRef(){
        Link ref = get(SELF);
        if(ref != null){
            return ref;
        }
        ref = new Link(Stapler.getCurrentRequest().getPathInfo());
        put(SELF, ref);
        return ref;
    }

    /**
     * @param name method name such as doXyz or getXyz
     *
     * @return for name doXyz or getXyz, gives xyz. For other than 'get' or 'do' prefix, gives lowercased method name
     */
    private Link createLinkRef(String name){
        return new Link(String.format("%s%s/",getOrCreateSelfRef().getHref(), name));
    }

    private String getPathFromMethodName(String methodName){
        if(methodName.startsWith("get")){
            return methodName.substring(3).toLowerCase();
        }else if(methodName.startsWith("do")){
            return methodName.substring(2).toLowerCase();
        }else{
            return "";
        }
    }
}


