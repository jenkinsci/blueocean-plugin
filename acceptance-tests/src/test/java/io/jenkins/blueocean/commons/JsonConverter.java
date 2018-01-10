package io.jenkins.blueocean.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;

/**
 * @author Vivek Pandey
 **/
public class JsonConverter{
    private static  final Logger LOGGER = LoggerFactory.getLogger(JsonConverter.class);
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final ObjectMapper om = createObjectMapper();

    public static <T> T toJava(String data, Class<T> type) {

        return toJava(new StringReader(data), type);
    }

    public static <T> T toJava(InputStream data, Class<T> type) {
        return toJava(new InputStreamReader(data, Charsets.UTF_8), type);
    }

    public static <T> T toJava(Reader data, Class<T> type) {
        try {
            return om.readValue(data, type);
        } catch (JsonParseException e){
            String msg = "Json parsing failure: "+e.getMessage();
            LOGGER.error(msg, e);
            throw new RuntimeException("Json parsing failure: "+e.getMessage());
        } catch (JsonMappingException e){
            String msg = String.format("Failed to map Json to java type : %s. %s ", type, e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg);
        } catch (IOException e) {
            String msg = String.format("Failed to convert %s to type %s", data, type);
            LOGGER.error(msg, e);
            throw new RuntimeException(msg);
        }
    }

    public static String toJson(Object value) {
        try {
            return om.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to convert %s to json", value.toString()), e);
        }
    }

    private static ObjectMapper createObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_STRING));
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
