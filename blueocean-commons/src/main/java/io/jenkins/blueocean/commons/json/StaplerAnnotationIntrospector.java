package io.jenkins.blueocean.commons.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.lang.ClassUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

class StaplerAnnotationIntrospector extends JacksonAnnotationIntrospector {

    /**
     * Discovers the name of the property if annotated with {@link Exported#name()}
     */
    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        Exported export = _findAnnotation(a, Exported.class);
        if (export == null) {
            return null;
        }
        if (!Strings.isNullOrEmpty(export.name())) {
            return PropertyName.construct(export.name());
        }
        return PropertyName.USE_DEFAULT;
    }

    /**
     * Ignores all types that are not primitives, {@link String} or annotated with {@link ExportedBean}
     */
    @Override
    public Boolean isIgnorableType(AnnotatedClass ac) {
        return !ac.getRawType().isPrimitive() && !ac.getRawType().equals(String.class) && !Map.class.isAssignableFrom(ac.getRawType()) && !_hasAnnotation(ac, ExportedBean.class);
    }

    /**
     * Merges properties when annoated with {@link Exported#merge()}
     */
    @Override
    public boolean hasAnyGetterAnnotation(AnnotatedMethod am) {
        Exported export = _findAnnotation(am, Exported.class);
        return super.hasAnyGetterAnnotation(am) || export != null && export.merge();
    }

    /**
     * Excludes properties if they have been annotated with {@link Exported#skipNull()}
     */
    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        Exported export = _findAnnotation(a, Exported.class);
        if (export != null) {
            if (export.skipNull()) {
                return JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.USE_DEFAULTS);
            }
        }
        return null;
    }

    @Override
    public JsonIgnoreProperties.Value findPropertyIgnorals(Annotated a) {
        return super.findPropertyIgnorals(a);
//        Set<String> toIgnore = Sets.newHashSet();
//        for (Method method : a.getRawType().getMethods()) {
//            Exported exported = method.getAnnotation(Exported.class);
//            if (exported == null) toIgnore.add(Introspector.decapitalize(method.getName().substring(method.getName().startsWith("is") ? 2 : 3)));
//        }
//        for (Field field : a.getRawType().getFields()) {
//            Exported exported = field.getAnnotation(Exported.class);
//            if (exported == null) toIgnore.add(field.getName());
//        }
//        return JsonIgnoreProperties.Value.forIgnoredProperties(toIgnore);
    }
}
