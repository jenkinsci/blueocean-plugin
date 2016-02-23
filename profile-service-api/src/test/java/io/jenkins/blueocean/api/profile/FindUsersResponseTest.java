package io.jenkins.blueocean.api.profile;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.profile.model.User;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class FindUsersResponseTest {
    @Test
    public void serializeDeserialize(){
        FindUsersResponse response = new FindUsersResponse(
                ImmutableList.of(new User("123", "john"),
                        new User("124", "alice")), null, null);

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        FindUsersResponse responseFromJson = JsonConverter.toJava(json, FindUsersResponse.class);



        for(int i =0; i<response.users.size(); i++){
            User expected = response.users.get(i);
            User actual = responseFromJson.users.get(i);
            Assert.assertEquals(expected.fullName, actual.fullName);
            Assert.assertEquals(expected.id, actual.id);
        }

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }

}
