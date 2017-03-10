package io.jenkins.blueocean.commons.stapler.export;

import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Allows caller to intercept exporting of properties.
 *
 * Implementation can choose to ignore properties in case of failure during serialization.
 *
 * @author Vivek Pandey
 * @author James Dumay
 */
public abstract class ExportInterceptor {
    /**
     * Constant to tell if return of {@link ExportInterceptor#getValue(Property, Object, ExportConfig)} should be skipped.
     *
     * Constant to skip serializaing a property in case of error
     */
    public static final Object SKIP = new Object();

    /**
     * Subclasses must call {@link Property#getValue(Object)}  to retrieve the property.
     *
     * If the subclass decides the value can be included in the request return the value
     * otherwise, return {@link #SKIP}  to skip the property.
     *
     * @param property to get the value from model object
     * @param model object with this property
     * @return the value of the property, if {@link #SKIP} is returned, this property will be skipped
     * @throws IOException if there was a problem with serialization that should prevent
     *         the serialization from proceeding
     * @see Exported#skipNull()
     */
    public abstract Object getValue(Property property, Object model, ExportConfig config) throws IOException;

    public static final ExportInterceptor DEFAULT = new ExportInterceptor() {
        @Override
        public Object getValue(Property property, Object model, ExportConfig config) throws IOException {
            try {
                return property.getValue(model);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if(config.isSkipIfFail()) {
                    return SKIP;
                }
                throw new IOException("Failed to write " + property.name + ":" + e.getMessage(), e);
            }
        }
    };
}
