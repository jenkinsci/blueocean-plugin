package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@ExportedBean()
public class ExtensionClassImpl extends BlueExtensionClass {
    private final Iterator<Object>  classes;

    public ExtensionClassImpl(Class baseClass) {
        List<Object> classes = new ArrayList<>();
        collectSuperInterfaces(classes,baseClass);
        this.classes = classes.iterator();
    }

    private void collectSuperInterfaces(List<Object> classes, Class base){
        for(Class c:base.getInterfaces()){
            if(classes.contains(c.getName())){
                continue;
            }
            classes.add(c.getName());
            collectSuperInterfaces(classes,base);

            Class clz = c.getSuperclass();
            if(clz != null && !classes.contains(c.getName())){
                classes.add(clz.getName());
                collectSuperInterfaces(classes,clz);
            }
        }
    }

//    @Override
//    public boolean hasNext() {
//        return classes.hasNext();
//    }
//
//    @Override
//    public String next() {
//        return classes.next();
//    }
//
//    @Override
//    public void remove() {
//        //NOP
//    }

    @Override
    public Iterator<Object> iterator() {
        return classes;
    }
}
