package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.commons.ServiceException;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class Utils {
    @SuppressWarnings("unchecked")
    public static <T> T cast(String value, Class<T> type){
        try{
            if(String.class.isAssignableFrom(type)){
                return (T)value;
            }else if(Integer.class.isAssignableFrom(type)){
                return (T)Integer.valueOf(value);
            }else if(Long.class.isAssignableFrom(type)){
                return (T)Long.valueOf(value);
            }else if(Boolean.class.isAssignableFrom(type)){
                return (T)Boolean.valueOf(value);
            }else if(List.class.isAssignableFrom(type)){ //List<String>
                String[] vs = value.split(",");
                List<String> list = new ArrayList<>();
                for(String v: vs){
                    list.add(v.trim());
                }
                return (T) Collections.singletonList(list);
            }else if(Set.class.isAssignableFrom(type)){ //List<String>
                String[] vs = value.split(",");
                Set<String> set = new HashSet<>();
                for(String v: vs){
                    set.add(v.trim());
                }
                return (T) Collections.singleton(set);
            }else{
                throw new ServiceException.UnexpectedErrorException(
                    String.format("Unknown type %s", type));
            }
        }catch (NumberFormatException e){
            throw new ServiceException.BadRequestException(
                String.format("Value %s can't be converted to type: %s", value, type));
        }
    }

    public static String ensureTrailingSlash(@NonNull String path){
        return path.charAt(path.length()-1) == '/' ? path : path+"/";
    }

    public static <T> int skip(Iterator<T> base, int offset){
        Objects.requireNonNull(base);
        if(offset < 0) throw new IllegalArgumentException("offest must be >= )");
        int i;
        for (i = 0; i < offset && base.hasNext(); i++) {
            base.next();
        }
        return i;
    }
}
