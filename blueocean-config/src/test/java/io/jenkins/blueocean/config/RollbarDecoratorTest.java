package io.jenkins.blueocean.config;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RollbarDecoratorTest  {

    @Test
    public void assertDefaultValue() {
        // it's a static initialized field - not many possibilities to test
        assertThat(new RollbarDecorator().isRollBarEnabled(), equalTo(false));
    }
}
