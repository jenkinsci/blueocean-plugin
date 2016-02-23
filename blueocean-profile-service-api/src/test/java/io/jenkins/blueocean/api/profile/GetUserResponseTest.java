package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.api.profile.model.User;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class GetUserResponseTest {
    @Test
    public void serializeDeserialize(){
        GetUserResponse response = new GetUserResponse(new User("123", "John"));

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        GetUserResponse responseFromJson = JsonConverter.toJava(json, GetUserResponse.class);


        Assert.assertEquals(response.user.fullName, responseFromJson.user.fullName);
        Assert.assertEquals(response.user.id, responseFromJson.user.id);

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }

}
