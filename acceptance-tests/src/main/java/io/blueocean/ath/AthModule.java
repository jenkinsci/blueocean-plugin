package io.blueocean.ath;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.factory.*;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class AthModule extends AbstractModule {
    @Override
    protected void configure() {
        // Can run this locally with system properties, e.g.:
        // -DwebDriverType=chrome -DwebDriverUrl=http://localhost:4444 -DwebDriverBrowserSize=800x600 -DjenkinsUrl=http://localhost:8080/jenkins
        String webDriverType = System.getProperty("webDriverType");
        DesiredCapabilities capability;
        if ("chrome".equals(webDriverType)) {
            capability = DesiredCapabilities.chrome();
        } else {
            capability = DesiredCapabilities.firefox();
        }

        String webDriverUrl = System.getProperty("webDriverUrl", "http://localhost:4444/wd/hub");
        String webDriverBrowserSize = System.getProperty("webDriverBrowserSize");

        try {
            WebDriver driver = new RemoteWebDriver(new URL(webDriverUrl), capability);
            LocalDriverElement.CURRENT_WEB_DRIVER.set(driver);

            driver = new Augmenter().augment(driver);
            if (webDriverBrowserSize == null) {
                driver.manage().window().maximize();
            } else {
                String[] widthXHeight = webDriverBrowserSize.split("x");
                driver.manage().window().setSize(new Dimension(Integer.parseInt(widthXHeight[0]), Integer.parseInt(widthXHeight[1])));
            }
            driver.manage().deleteAllCookies();
            bind(WebDriver.class).toInstance(driver);

            String launchUrl = System.getProperty("jenkinsUrl");
            if (launchUrl == null) {
                launchUrl = new String(Files.readAllBytes(Paths.get("runner/.blueocean-ath-jenkins-url")));
            }
            bindConstant().annotatedWith(BaseUrl.class).to(launchUrl);

            CustomJenkinsServer server;
            if (System.getProperty("adminUsername") != null) {
                server = new CustomJenkinsServer(new URI(launchUrl), System.getProperty("adminUsername"), System.getProperty("adminPassword"));
            } else {
                server = new CustomJenkinsServer(new URI(launchUrl));
            }
            bind(JenkinsServer.class).toInstance(server);
            bind(CustomJenkinsServer.class).toInstance(server);

            if(server.getComputerSet().getTotalExecutors() < 10) {
                server.runScript(
                    "jenkins.model.Jenkins.getInstance().setNumExecutors(10);\n" +
                        "jenkins.model.Jenkins.getInstance().save();\n");
            }

            Properties properties = new Properties();
            File liveProperties = new File("live.properties");
            if (liveProperties.canRead()) {
                properties.load(new FileInputStream(liveProperties));
            }
            bind(Properties.class).annotatedWith(Names.named("live")).toInstance(properties);
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
            .implement(ClassicPipeline.class, ClassicPipeline.class)
            .build(ClassicPipelineFactory.class));

        install(new FactoryModuleBuilder()
            .implement(RunDetailsPipelinePage.class, RunDetailsPipelinePage.class)
            .build(RunDetailsPipelinePageFactory.class));

        install(new FactoryModuleBuilder()
            .implement(RunDetailsArtifactsPage.class, RunDetailsArtifactsPage.class)
            .build(RunDetailsArtifactsPageFactory.class));


        install(new FactoryModuleBuilder()
            .implement(BranchPage.class, BranchPage.class)
            .build(BranchPageFactory.class));
    }
}
