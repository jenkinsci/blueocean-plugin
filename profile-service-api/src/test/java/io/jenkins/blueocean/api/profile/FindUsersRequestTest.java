package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class FindUsersRequestTest {
    @Test
    public void serializeDeserialize(){
        FindUsersRequest request = new FindUsersRequest("cloudbees", 1L, 25L);

        String json = JsonConverter.toJson(request);

        System.out.println("Converted from Java:\n"+json);

        FindUsersRequest requestFromJson = JsonConverter.toJava(json,
                FindUsersRequest.class);

        Assert.assertEquals(request.organization, requestFromJson.organization);
        Assert.assertEquals(request.start, requestFromJson.start);
        Assert.assertEquals(request.limit, requestFromJson.limit);

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(requestFromJson));
    }

}
