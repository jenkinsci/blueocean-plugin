package io.blueocean.ath;

import com.google.inject.AbstractModule;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.pages.classic.ClassicFreestyleCreationPage;
import io.blueocean.ath.pages.classic.LoginPage;
import io.blueocean.ath.sse.SSEClient;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.net.URISyntaxException;

public class AthModule extends AbstractModule {
    final WebDriver driver;
    final String base;
    public AthModule(WebDriver driver, String base){
        this.driver = driver;
        this.base = base;
    }
    @Override
    protected void configure() {
        bindConstant().annotatedWith(BaseUrl.class).to(base);
        bind(WebDriver.class).toInstance(driver);
        bind(WaitUtil.class);
        bind(LoginPage.class);
        bind(ClassicJobApi.class);
        bind(ClassicFreestyleCreationPage.class);
        bind(DashboardPage.class);
        bind(GithubCreationPage.class);
        bind(SSEClient.class);

        try {
            bind(JenkinsServer.class).toInstance(new JenkinsServer(new URI(base)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
