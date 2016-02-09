package io.jenkins.blueocean.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vivek Pandey
 **/
public class JsonConverter{
    public static final ObjectMapper om = createObjectMapper();

    public static <T> T toJava(String data, Class<T> type) {
        try {
            return om.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Invalid json. Failed to convert json %s to type %s", data, type));
        }
    }

    public static <T> T toJava(InputStream data, Class<T> type) {
        try {
            return om.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Invalid json. Failed to convert %s to type %s", data, type));
        }
    }

    public static String toJson(Object value) {
        try {
            return om.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to convert %s to json", value.toString()));
        }
    }

    private static ObjectMapper createObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
