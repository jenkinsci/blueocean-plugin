package io.jenkins.blueocean.commons;


import com.fasterxml.jackson.databind.JsonDeserializer;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * Extension point to deserialize JSON to a custom Java type
 *
 * @author Vivek Pandey
 */
public abstract class JsonJacksonDeserializer implements ExtensionPoint {

    public abstract String getName();

    public abstract Class getType();

    public abstract JsonDeserializer getJsonDeserializer();

    public static ExtensionList<JsonJacksonDeserializer> all(){
        return ExtensionList.lookup(JsonJacksonDeserializer.class);
    }
}
