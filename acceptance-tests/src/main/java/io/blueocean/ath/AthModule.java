package io.blueocean.ath;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.BranchPageFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.EditorPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import io.blueocean.ath.pages.classic.ClassicFreestyleCreationPage;
import io.blueocean.ath.pages.classic.LoginPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AthModule extends AbstractModule {
    @Override
    protected void configure() {

        DesiredCapabilities capability = DesiredCapabilities.firefox();

        try {
            WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
            driver.manage().window().maximize();
            driver.manage().deleteAllCookies();
            bind(WebDriver.class).toInstance(driver);

            String launchUrl = new String(Files.readAllBytes(Paths.get("runner/.blueocean-ath-jenkins-url")));
            bindConstant().annotatedWith(BaseUrl.class).to(launchUrl);

            JenkinsServer server = new JenkinsServer(new URI(launchUrl));
            bind(JenkinsServer.class).toInstance(server);
            if(server.getComputerSet().getTotalExecutors() < 10) {
                server.runScript(
                    "jenkins.model.Jenkins.getInstance().setNumExecutors(10);\n" +
                        "jenkins.model.Jenkins.getInstance().save();\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }



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
        install(new FactoryModuleBuilder()
            .implement(BranchPage.class, BranchPage.class)
            .build(BranchPageFactory.class));
    }
}
