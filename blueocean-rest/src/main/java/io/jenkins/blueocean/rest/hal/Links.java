package io.jenkins.blueocean.rest.hal;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.model.Container;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.Exported;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public final class Links extends HashMap<String,Link>{
    private final Object self;

    private static final String SELF = "self";
    public Links(Object self) {
        this.self = self;
        getOrCreateSelfRef();
        populateReferences();
    }

    public Links add(String ref, Link link){
        put(ref, link);
        return this;
    }

    private void populateReferences(){
        Class clazz = self.getClass();
        /** Find if there is method returning a {@link Container}, add this as link */
        for (Method m : findMethods(clazz,clazz,new ArrayList<Method>())) {
            String p = getPathFromMethodName(m);
            put(p, createLinkRef(p, Stapler.getCurrentRequest().getPathInfo()));
        }
    }

    private List<Method> findMethods(Class c, Type logical, List<Method> r){
        Method m;
        for (Class i : c.getInterfaces()) {
            findMethods(i, Types.getBaseClass(logical, i), r);
        }

        Class sc = c.getSuperclass();
        if (sc!=null) {
            findMethods(sc, Types.getBaseClass(logical, sc), r);
        }

        for (Method method : c.getDeclaredMethods()) {
            if(method.getAnnotation(Navigable.class) != null){
                r.add(method);
            }
        }
        return r;
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
    private Link createLinkRef(String name, String base){
        base = ensureTrailingSlash(base);
        return new Link(String.format("%s%s/",base, name));
    }

    private String getPathFromMethodName(Method m){
        String methodName = m.getName();
        Exported exportedAnn = m.getAnnotation(Exported.class);
        if(exportedAnn != null && !exportedAnn.name().trim().isEmpty())
            return exportedAnn.name();
        if(methodName.startsWith("get")){
            return methodName.substring(3).toLowerCase();
        }else if(methodName.startsWith("do")){
            return methodName.substring(2).toLowerCase();
        }else{
            return "";
        }
    }

    private String getBasePath(){
        String path = Stapler.getCurrentRequest().getPathInfo();
        String contextPath = Stapler.getCurrentRequest().getContextPath().trim();

        if(!contextPath.isEmpty() || !contextPath.equals("/")){
            int i = path.indexOf(contextPath);
            if(i >= 0){
                if(path.length() > i){
                    int j = path.indexOf('/', i+1);
                    return path.substring(j);
                }
            }
        }
        return path;
    }

    private String ensureTrailingSlash(String path){
        if(path.charAt(path.length() - 1) != '/'){
           return path + "/";
        }
        return path;
    }
}


