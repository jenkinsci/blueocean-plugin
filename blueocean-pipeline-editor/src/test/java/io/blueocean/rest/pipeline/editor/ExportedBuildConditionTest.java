package io.blueocean.rest.pipeline.editor;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class ExportedBuildConditionTest {

    @Test
    public void getName() {
        MatcherAssert.assertThat(new ExportedBuildCondition("myName","myDescription").getName(),equalTo("myName"));
    }

    @Test
    public void getDescription() {
        MatcherAssert.assertThat(new ExportedBuildCondition("myName","myDescription").getDescription(),equalTo("myDescription"));    }
}
