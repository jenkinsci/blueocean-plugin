package io.blueocean.ath.offline.pipeline;

import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class ArtifactsTest extends BlueOceanAcceptanceTest implements WebDriverMixin {
    private Logger logger = Logger.getLogger(this.getClass());
    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    WaitUtil wait;

    @Test
    public void testArtifactsList() throws IOException {
        String script = loadResource("artifactsList.groovy");
        String pipelineName = this.getClass().getSimpleName() + "_testArtifactListTruncate";
        ClassicPipeline pipeline = pipelineFactory.pipeline(pipelineName).createPipeline(script).build();

        sseClientRule.untilEvents(SSEEvents.activityComplete(pipelineName));

        pipeline.getRunDetailsArtifactsPage().open(1);

        wait.until(By.className("btn-show-more"));
        click(".btn-show-more");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("JTable-row"), 130));
        logger.info("Found artifacts table");
    }
}
