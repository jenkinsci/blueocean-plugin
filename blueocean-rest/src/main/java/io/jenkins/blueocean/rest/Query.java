package io.jenkins.blueocean.rest;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.beanutils.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Query syntax
 *
 * Give all pipelines for acme organization
 *
 * <pre>/search?q=type:pipeline organization:acme</pre>
 *<p>
 * Give all Runs for pipelineA in organization acme
 *
 * <pre>/search?q=type:run pipeline:pipelineA organization:acme</pre>
 *
 * @author Vivek Pandey
 */
public class Query {

    public final String type;
    private final Map<String, String> params;

    private Query(String type, Map<String, String> values) {
        this.type = type;
        this.params = (values == null) ? ImmutableMap.<String, String>of() : ImmutableMap.copyOf(values);
    }

    public String param(String key){
        return params.get(key.toLowerCase());
    }

    public String param(String key, boolean required){
        return param(key, String.class, required);
    }

    public <T> T param(String key, Class<T> type){
        return param(key, type, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T param(String key, Class<T> type, boolean required){
        String value = params.get(key);
        if(value == null && required){
            throw new ServiceException.BadRequestException(
                String.format("%s is required parameter to execute query for type %s", key, type));
        }else if (value == null && Boolean.class.isAssignableFrom(type)){
            return (T) Boolean.FALSE;
        }else if (value == null){
            return null;
        }
        return Utils.cast(value, type);
    }

    public static Query parse(String query) {
        String[] values = query.split(";");

        String type = null;
        Map<String, String> params = new HashMap<>();
        for (String v : values) {
            String[] vv = v.split(":");
            if (vv.length != 2) {
                continue;
            }
            String key = vv[0].trim().toLowerCase();
            String value = vv[1].trim();
            if (key.equals("type")) {
                type = value.toLowerCase();
            } else {
                params.put(key, value);
            }
        }
        //If there is no type, we don't know what resource to look for
        //TODO: what would be default type?
        if (type == null) {
            throw new ServiceException.BadRequestException("type is required parameter for query param q");
        }
        return new Query(type, params);
    }

    public static class StaplerConverterImpl implements Converter {
        public Object convert(Class type, Object value) {
            if (value==null)
                return null;
            if (value instanceof String) {
                return parse((String) value);
            }
            throw new UnsupportedOperationException();
        }
    }
}
