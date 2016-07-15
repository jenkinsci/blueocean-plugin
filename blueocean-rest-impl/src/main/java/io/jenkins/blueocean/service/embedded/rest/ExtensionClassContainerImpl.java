package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import io.jenkins.blueocean.rest.model.BlueExtensionClassContainer;
import io.jenkins.blueocean.rest.model.BlueExtensionClassMap;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Extension
public class ExtensionClassContainerImpl extends BlueExtensionClassContainer {

    @Override
    public BlueExtensionClass get(String name) {
        return new ExtensionClassImpl(getClazz(name), this);
    }

//    @Override
//    public GenericResource<Map<String, BlueExtensionClass>> map(@QueryParameter("q") String param) {
//        if(param == null){
//            return new GenericResource<Map<String, BlueExtensionClass>>(Collections.<String, BlueExtensionClass>emptyMap());
//        }
//        final Map<String, BlueExtensionClass> classMap = new HashMap<>();
//        for(String p:param.split(",")){
//            p = p.trim();
//            classMap.put(p, new ExtensionClassImpl(getClazz(p), this));
//        }
//
//        return new GenericResource<>(classMap);
//    }

    @Override
    public BlueExtensionClassMap getMap(@QueryParameter("q") final String param) {
        if(param == null){
            final Map map = Collections.EMPTY_MAP;
//            return new BlueExtensionClassMap() {
//                @Override
//                public int size() {
//                    return map.size();
//                }
//
//                @Override
//                public boolean isEmpty() {
//                    return map.isEmpty();
//                }
//
//                @Override
//                public boolean containsKey(Object key) {
//                    return false;
//                }
//
//                @Override
//                public boolean containsValue(Object value) {
//                    return false;
//                }
//
//                @Override
//                public BlueExtensionClass get(Object key) {
//                    return null;
//                }
//
//                @Override
//                public BlueExtensionClass put(String key, BlueExtensionClass value) {
//                    return null;
//                }
//
//                @Override
//                public BlueExtensionClass remove(Object key) {
//                    return null;
//                }
//
//                @Override
//                public void putAll(Map<? extends String, ? extends BlueExtensionClass> m) {
//
//                }
//
//                @Override
//                public void clear() {
//
//                }
//
//                @Override
//                public Set<String> keySet() {
//                    return null;
//                }
//
//                @Override
//                public Collection<BlueExtensionClass> values() {
//                    return null;
//                }
//
//                @Override
//                public Set<Entry<String, BlueExtensionClass>> entrySet() {
//                    return new HashSet<>();
//                }
//
////                @Override
////                public Map<String, BlueExtensionClass> get() {
////                    return Collections.emptyMap();
////                }
//            };
        }

        final Map<String, BlueExtensionClass> classMap = new HashMap<>();
        for(String p:param.split(",")){
            p = p.trim();
            classMap.put(p, new ExtensionClassImpl(getClazz(p), this));
        }

//        return classMap;
        return new BlueExtensionClassMap() {
            @Override
            public Link getLink() {
                return ExtensionClassContainerImpl.this.getLink().rel("?q="+param);
            }

            @Override
            public Map<String, BlueExtensionClass> getMap() {
                return classMap;
            }
        };

//        return new BlueExtensionClassMap(){
//
//            @Override
//            public int size() {
//                return classMap.size();
//            }
//
//            @Override
//            public boolean isEmpty() {
//                return classMap.isEmpty();
//            }
//
//            @Override
//            public boolean containsKey(Object key) {
//                return classMap.containsKey(key);
//            }
//
//            @Override
//            public boolean containsValue(Object value) {
//                return classMap.containsValue(value);
//            }
//
//            @Override
//            public BlueExtensionClass get(Object key) {
//                return classMap.get(key);
//            }
//
//            @Override
//            public BlueExtensionClass put(String key, BlueExtensionClass value) {
//                return null;
//            }
//
//            @Override
//            public BlueExtensionClass remove(Object key) {
//                return null;
//            }
//
//            @Override
//            public void putAll(Map<? extends String, ? extends BlueExtensionClass> m) {
//
//            }
//
//            @Override
//            public void clear() {
//
//            }
//
//            @Override
//            public Set<String> keySet() {
//                return classMap.keySet();
//            }
//
//            @Override
//            public Collection<BlueExtensionClass> values() {
//                return classMap.values();
//            }
//
//            @Override
//            public Set<Entry<String, BlueExtensionClass>> entrySet() {
//                return classMap.entrySet();
//            }
//
////            @Override
////            public Map<String, BlueExtensionClass> get() {
////                return classMap;
////            }
//        };

    }

    @Override
    public Link getLink() {
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }

    private Class getClazz(String name){
        try {
            return this.getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ServiceException.NotFoundException(String.format("Class %s is not known", name));
        }
    }
}


