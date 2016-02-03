package io.jenkins.blueocean.commons;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * Abstraction to represent login details of an authentication provider
 *
 * @author Vivek Pandey
 */

@JsonSerialize(using = LoginDetails.LoginDetailsSerializer.class)
@JsonDeserialize(using = LoginDetails.LoginDetailsDeserializer.class)
public class LoginDetails{

    private final Map<String, Object> config;

    /**
     * @param key key
     * @param type expected value of this type
     * @param <T> expected type
     * @return  value of type T. Maybe null if no value found corresponding to
     * @throws ClassCastException if no such
     */
    @SuppressWarnings("unchecked")
    @Nullable public <T> T get(@Nonnull String key, Class<T> type){
        Object o =  config.get(key);
        if(o!=null){
            if(type.isInstance(o)) {
                return (T) o;
            }else{
                throw new ClassCastException(String.format("No instance of type %s found for key %s",type, key));
            }
        }
        return null;
    }


    /**
     * Construct using Map.
     *
     * @param config map containing authentication provider specific configuration
     */
    public LoginDetails(@Nonnull Map<String, Object> config){
        this.config = ImmutableMap.copyOf(config);
    }

    /**
     * JSON serializer of LoginDetails
     *
     * Expected serialized form is:
     *
     * {
     *     "accessToken": "aaaaaa",
     *     "someOtherConfig":{
     *         ...
     *     }
     * }
     */
    public static class LoginDetailsSerializer extends JsonSerializer<LoginDetails>{
        @Override
        public void serialize(LoginDetails value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
            jgen.writeObject(value.config);
        }
    }

    @SuppressWarnings("unchecked")
    public static class LoginDetailsDeserializer extends JsonDeserializer<LoginDetails>{
        @Override
        public LoginDetails deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException{
            Map node = jp.getCodec().readValue(jp, Map.class);
            return new LoginDetails(node);
        }
    }
}
