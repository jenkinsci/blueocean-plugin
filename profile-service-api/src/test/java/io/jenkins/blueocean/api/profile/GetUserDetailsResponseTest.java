package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class GetUserDetailsResponseTest {
    @Test
    public void serializeDeserialize(){
        GetUserDetailsResponse response = new GetUserDetailsResponse(new UserDetails("123", "John", "john@example.com", "121212", "github"));

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        GetUserDetailsResponse responseFromJson = JsonConverter.toJava(json, GetUserDetailsResponse.class);


        Assert.assertEquals(response.userDetails.id, responseFromJson.userDetails.id);
        Assert.assertEquals(response.userDetails.name, responseFromJson.userDetails.name);
        Assert.assertEquals(response.userDetails.email, responseFromJson.userDetails.email);
        Assert.assertEquals(response.userDetails.accessToken, responseFromJson.userDetails.accessToken);
        Assert.assertEquals(response.userDetails.authProvider, responseFromJson.userDetails.authProvider);

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }

}
