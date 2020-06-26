package io.blueocean.ath.offline.pipeline;

import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ATHJUnitRunner.class)
public class FakeRunDetailsTest extends BlueOceanAcceptanceTest implements WebDriverMixin {

    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    @Rule
    public SSEClientRule sseClient;

    @Test
    public void downstreamJobLinkAppearsInRunResult() throws Exception {

        final String pipelineJobScript = "stage ('stageName') { echo 'blah' }";
        ClassicPipeline pipelineJob = pipelineFactory.pipeline("pipelineJob").createPipeline(pipelineJobScript);

        pipelineJob.build();
        sseClient.untilEvents(pipelineJob.buildsFinished);
        sseClient.clear();

        pipelineJob.getRunDetailsFakePage().open(1);

        find("//*[contains(text(),'Fake Run Details')]").isVisible(); // Fake page content
    }
}
