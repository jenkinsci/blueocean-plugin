package io.jenkins.blueocean.api.pipeline;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class FindPipelineRunsRequestTest {

    @Test
    public void serializeDeserialize(){
        FindPipelineRunsRequest request = new FindPipelineRunsRequest("cloudbees", "test1", true,
                ImmutableList.of("master"), null, null);

        String json = JsonConverter.toJson(request);

        System.out.println("Converted from Java:\n"+json);

        FindPipelineRunsRequest requestFromJson = JsonConverter.toJava(json,
                FindPipelineRunsRequest.class);

        Assert.assertEquals(request.latestOnly, requestFromJson.latestOnly);
        Assert.assertEquals(request.organization, requestFromJson.organization);
        Assert.assertEquals(request.pipeline, requestFromJson.pipeline);
        Assert.assertArrayEquals(request.branches.toArray(), requestFromJson.branches.toArray());

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(requestFromJson));
    }
}
