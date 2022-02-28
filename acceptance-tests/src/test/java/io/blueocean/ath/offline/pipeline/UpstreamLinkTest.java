package io.blueocean.ath.offline.pipeline;

import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class UpstreamLinkTest extends BlueOceanAcceptanceTest implements WebDriverMixin {
    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    FreestyleJobFactory freestyleJobFactory;

    @Inject @Rule
    public SSEClientRule sseClient;


    @Test
    public void upstreamLinkTest() throws IOException {
        String jenkinsFile = loadJenkinsFile();

        FreestyleJob upstreamJob = freestyleJobFactory.pipeline("upstreamJob").create("echo blah");

        ClassicPipeline downstreamJob = pipelineFactory.pipeline("downstreamJob")
            .createPipeline(jenkinsFile)
            .build();

        sseClient.untilEvents(downstreamJob.buildsFinished);
        sseClient.clear();

        upstreamJob.build();
        sseClient.untilEvents(upstreamJob.buildsFinished);
        sseClient.untilEvents(downstreamJob.buildsFinished);
        sseClient.clear();

        ActivityPage activityPage = downstreamJob.getActivityPage().open();

        find("//*[contains(text(),'Started by upstream pipeline')]").isVisible();

        downstreamJob.getRunDetailsPipelinePage().open(2);

        find("//*[contains(text(),'Started by upstream pipeline')]").isVisible();
    }

}
