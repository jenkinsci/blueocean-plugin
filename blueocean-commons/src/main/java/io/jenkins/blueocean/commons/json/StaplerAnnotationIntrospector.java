package io.jenkins.blueocean.commons.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.google.common.base.Strings;
import org.kohsuke.stapler.export.Exported;

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
        return super.findPropertyInclusion(a);
    }

    @Override
    protected boolean _isIgnorable(Annotated a) {
        return !_hasAnnotation(a, Exported.class) || super._isIgnorable(a);
    }
}
