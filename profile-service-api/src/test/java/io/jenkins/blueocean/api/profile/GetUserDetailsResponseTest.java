package io.jenkins.blueocean.api.profile;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.LoginDetails;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GetUserDetailsResponseTest {
    @Test
    public void serializeDeserialize(){

        String accessToken = "1212121212";
        String refreshToken = "ababababa";
        Map<String,Object> gconfig = ImmutableMap.<String,Object>of("accessToken", accessToken, "refreshToken", refreshToken);
        GetUserDetailsResponse response = new GetUserDetailsResponse(new UserDetails("123", "John", "john@example.com",
                ImmutableMap.of("github",new LoginDetails(ImmutableMap.<String, Object>of("accessToken", accessToken)),
                        "google",new LoginDetails(gconfig))));
        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        GetUserDetailsResponse responseFromJson = JsonConverter.toJava(json, GetUserDetailsResponse.class);


        Assert.assertEquals(response.userDetails.id, responseFromJson.userDetails.id);
        Assert.assertEquals(response.userDetails.name, responseFromJson.userDetails.name);
        Assert.assertEquals(response.userDetails.email, responseFromJson.userDetails.email);
        LoginDetails ld = response.userDetails.getLoginDetails("github");
        Assert.assertNotNull(ld);
        Assert.assertEquals(ld.get("accessToken", String.class), accessToken);

        LoginDetails gld = response.userDetails.getLoginDetails("google");
        Assert.assertNotNull(gld);
        Assert.assertEquals(gld.get("accessToken", String.class), accessToken);
        Assert.assertEquals(gld.get("refreshToken", String.class), refreshToken);

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }

}
