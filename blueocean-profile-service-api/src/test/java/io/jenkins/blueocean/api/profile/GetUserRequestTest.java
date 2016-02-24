package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class GetUserRequestTest {
    @Test
    public void serializeDeserialize(){
        GetUserRequest request = new GetUserRequest("123");

        String json = JsonConverter.toJson(request);

        System.out.println("Converted from Java:\n"+json);

        GetUserRequest requestFromJson = JsonConverter.toJava(json, GetUserRequest.class);


        Assert.assertEquals(request.id, requestFromJson.id);
        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(requestFromJson));
    }
}
