package io.jenkins.blueocean.api.pipeline;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class GetPipelineResponseTest {
    @Test
    public void serializeDeserialize(){
        GetPipelineResponse response = new GetPipelineResponse(new Pipeline("cloudbees", "test1",
                ImmutableList.of("master", "qa")));

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        GetPipelineResponse responseFromJson = JsonConverter.toJava(json, GetPipelineResponse.class);


        Assert.assertEquals(response.pipeline.name, responseFromJson.pipeline.name);
        Assert.assertEquals(response.pipeline.organization, responseFromJson.pipeline.organization);
        Assert.assertArrayEquals(response.pipeline.branches.toArray(), responseFromJson.pipeline.branches.toArray());

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }
}
