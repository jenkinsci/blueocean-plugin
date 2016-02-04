package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class GetOrganizationRequestTest {
    @Test
    public void serializeDeserialize(){
        GetOrganizationRequest request = new GetOrganizationRequest("cloudbees");

        String json = JsonConverter.toJson(request);

        System.out.println("Converted from Java:\n"+json);

        GetOrganizationRequest requestFromJson = JsonConverter.toJava(json, GetOrganizationRequest.class);


        Assert.assertEquals(request.name, requestFromJson.name);
        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(requestFromJson));
    }
}
