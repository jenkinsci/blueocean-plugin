package io.jenkins.blueocean.api.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.api.pipeline.model.Result;
import io.jenkins.blueocean.api.pipeline.model.Run;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author Vivek Pandey
 */
public class FindPipelineRunsResponseTest {
    @Test
    public void serializeDeserialize(){
        FindPipelineRunsResponse response = new FindPipelineRunsResponse(
                ImmutableList.of(new Run.Builder("23","test1","cloudbees")
                .branch("master")
                .commitId("ccaaa22")
                .status(Run.Status.SUCCESSFUL)
                .enQueueTime(new Date())
                .startTime(new Date())
                .endTime(new Date(new Date().getTime() - 10000))
                .durationInMillis(10000L)
                .runSummary("build sucessful")
                .runTrend(Run.RunTrend.FIXED)
                .result(new Result("job", ImmutableMap.of("status", "success")))
                .build(),
                new Run.Builder("24","test1","cloudbees")
                        .branch("master")
                        .commitId("ccaaa22")
                        .status(Run.Status.SUCCESSFUL)
                        .startTime(new Date())
                        .enQueueTime(new Date())
                        .endTime(new Date(new Date().getTime() - 10000))
                        .durationInMillis(10000L)
                        .runSummary("build sucessful")
                        .runTrend(Run.RunTrend.FIXED)
                        .result(new Result("job",ImmutableMap.of("status", "success")))
                        .build()), null, null);

        String json = JsonConverter.toJson(response);

        System.out.println("Converted from Java:\n"+json);

        FindPipelineRunsResponse responseFromJson = JsonConverter.toJava(json,
                FindPipelineRunsResponse.class);


        Assert.assertEquals(response.previous, responseFromJson.previous);
        Assert.assertEquals(response.next, responseFromJson.next);

        for(int i =0; i<response.runs.size(); i++){
            Run expected = response.runs.get(i);
            Run actual = responseFromJson.runs.get(i);
            Assert.assertEquals(expected.id, actual.id);
            Assert.assertEquals(expected.pipeline, actual.pipeline);
            Assert.assertEquals(expected.organization, actual.organization);
            Assert.assertEquals(expected.startTime.getTime(), actual.startTime.getTime());
            Assert.assertEquals(expected.endTime.getTime(), actual.endTime.getTime());
            Assert.assertEquals(expected.durationInMillis, actual.durationInMillis);
            Assert.assertEquals(expected.enQueueTime, actual.enQueueTime);
            Assert.assertEquals(expected.runSummary, actual.runSummary);
            Assert.assertEquals(expected.runTrend, actual.runTrend);
//            Assert.assertTrue(actual.result instanceof JobResult);
        }

        System.out.println("Converted back from Json:\n"+JsonConverter.toJson(responseFromJson));
    }
}
