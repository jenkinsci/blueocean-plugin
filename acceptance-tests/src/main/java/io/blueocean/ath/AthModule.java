package io.blueocean.ath;

import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.EditorPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import io.blueocean.ath.pages.classic.ClassicFreestyleCreationPage;
import io.blueocean.ath.pages.classic.LoginPage;
import io.blueocean.ath.sse.SSEClient;
import org.jukito.JukitoModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AthModule extends JukitoModule
{
    WebDriver driver;


    @Override
    protected void configureTest() {

        DesiredCapabilities capability = DesiredCapabilities.firefox();
        try {
            driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        driver.manage().window().maximize();
        //driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();
        String launchUrl = "";
        try {
            launchUrl = new String(Files.readAllBytes(Paths.get("runner/.blueocean-ath-jenkins-url")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        bindConstant().annotatedWith(BaseUrl.class).to(launchUrl);
        bind(WebDriver.class).toInstance(driver);
        bind(WaitUtil.class);
        bind(LoginPage.class);
        bind(ClassicJobApi.class);
        bind(ClassicFreestyleCreationPage.class);
        bind(DashboardPage.class);
        bind(GithubCreationPage.class);
        bind(GitRepositoryRule.class);
        bind(ActivityPage.class);
        bind(EditorPage.class);

        install(new FactoryModuleBuilder()
            .implement(ActivityPage.class, ActivityPage.class)
            .build(ActivityPageFactory.class));

        install(new FactoryModuleBuilder()
            .implement(MultiBranchPipeline.class, MultiBranchPipeline.class)
            .build(MultiBranchPipelineFactory.class));

        install(new FactoryModuleBuilder()
            .implement(FreestyleJob.class, FreestyleJob.class)
            .build(FreestyleJobFactory.class));

        install(new FactoryModuleBuilder()
            .implement(RunDetailsPipelinePage.class, RunDetailsPipelinePage.class)
            .build(RunDetailsPipelinePageFactory.class));


        bind(SSEClient.class);
        try {
            bind(JenkinsServer.class).toInstance(new JenkinsServer(new URI(launchUrl)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
