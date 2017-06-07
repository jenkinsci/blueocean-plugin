package io.jenkins.blueocean.rest;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.jenkins.blueocean.commons.ServiceException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
                return (T)ImmutableList.of(list);
            }else if(Set.class.isAssignableFrom(type)){ //List<String>
                String[] vs = value.split(",");
                Set<String> set = new HashSet<>();
                for(String v: vs){
                    set.add(v.trim());
                }
                return (T) ImmutableSet.of(set);
            }else{
                throw new ServiceException.UnexpectedErrorException(
                    String.format("Unknown type %s", type));
            }
        }catch (NumberFormatException e){
            throw new ServiceException.BadRequestException(
                String.format("Value %s can't be converted to type: %s", value, type));
        }
    }

    public static String ensureTrailingSlash(@Nonnull String path){
        return path.charAt(path.length()-1) == '/' ? path : path+"/";
    }

    // We can't use guava Iterators.skip() as it's removed ver 13.0 onwards and core
    // enforces ver 11.0.1. So we use our own
    public static <T> int skip(Iterator<T> base, int offset){
        Preconditions.checkArgument(base != null);
        Preconditions.checkArgument(offset >= 0);
        int i;
        for (i = 0; i < offset && base.hasNext(); i++) {
            base.next();
        }
        return i;
    }
}
