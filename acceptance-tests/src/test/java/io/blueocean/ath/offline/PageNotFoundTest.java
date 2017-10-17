package io.blueocean.ath.offline;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.WaitUtil;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

@RunWith(ATHJUnitRunner.class)
public class PageNotFoundTest extends BlueOceanAcceptanceTest {
    //const urls = ['/blue/gibtEsNicht', '/blue/organizations/jenkins/gibtEsNicht/activity/', '/blue/organizations/gibtEsNicht/gibtEsNicht/detail/gibtEsNicht/'];

    private Logger logger = Logger.getLogger(PageNotFoundTest.class);

    @Inject
    WebDriver driver;

    @Inject
    WaitUtil wait;

    @Inject
    @BaseUrl
    String baseUrl;

    @Test
    public void testPageNotFound() {
        String[] urls = {
            "/blue/gibtEsNicht",
            "/blue/organizations/jenkins/gibtEsNicht/activity/",
            "/blue/organizations/gibtEsNicht/gibtEsNicht/detail/gibtEsNicht/"
        };

        for (String url : urls) {
            driver.get(baseUrl + url);

            wait.until(By.cssSelector("div.fullscreen.errorscreen.not-found"));
        }
    }
}
