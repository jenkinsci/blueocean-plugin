package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class CreateOrganizationRequestTest {
    @Test
    public void serializeDeserialize(){
        CreateOrganizationRequest request = new CreateOrganizationRequest("cloudbees");

        String json = JsonConverter.toJson(request);

        System.out.println("Converted from Java:\n"+json);

        CreateOrganizationRequest requestFromJson = JsonConverter.toJava(json, CreateOrganizationRequest.class);


        Assert.assertEquals(request.name, requestFromJson.name);
        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(requestFromJson));
    }

}
