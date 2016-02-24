package io.jenkins.blueocean.api.pipeline;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class FindPipelinesResponseTest {
    @Test
    public void serializeDeserialize(){
        FindPipelinesResponse response = new FindPipelinesResponse(
                ImmutableList.of(new Pipeline("cloudbees", "test1",
                ImmutableList.of("master", "qa")), new Pipeline("cloudbees", "test2",
                ImmutableList.of("master", "dev"))));

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        FindPipelinesResponse responseFromJson = JsonConverter.toJava(json,
                FindPipelinesResponse.class);



        for(int i =0; i<response.pipelines.size(); i++){
            Pipeline expected = response.pipelines.get(i);
            Pipeline actual = responseFromJson.pipelines.get(i);
            Assert.assertEquals(expected.name, actual.name);
            Assert.assertEquals(expected.organization, actual.organization);
            Assert.assertArrayEquals(expected.branches.toArray(), actual.branches.toArray());

        }

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }
}
