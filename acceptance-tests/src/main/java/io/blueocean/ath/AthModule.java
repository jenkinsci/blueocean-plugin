package io.blueocean.ath;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.BranchPageFactory;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.factory.PullRequestsPageFactory;
import io.blueocean.ath.factory.RunDetailsArtifactsPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.factory.RunDetailsTestsPageFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.PullRequestsPage;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import io.blueocean.ath.pages.blue.RunDetailsTestsPage;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

public class AthModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(AthModule.class);

    @Override
    protected void configure() {
        Config cfg = new Config();
        File userConfig = new File(new File(System.getProperty("user.home")), ".blueocean-ath-config");
        if (userConfig.canRead()) {
            cfg.loadProps(userConfig);
        }
        bind(Config.class).toInstance(cfg);

        String webDriverType = cfg.getString("webDriverType");
        MutableCapabilities capability;
        if ("firefox".equals(webDriverType)) {
            capability = new FirefoxOptions();
        } else {
            capability = new ChromeOptions();
        }

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        capability.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        String webDriverUrl = cfg.getString("webDriverUrl", "http://localhost:4444/wd/hub");
        String webDriverBrowserSize = cfg.getString("webDriverBrowserSize");

        try {
            String launchUrl = cfg.getString("jenkinsUrl");
            if (launchUrl == null) {
                launchUrl = new String(Files.readAllBytes(Paths.get("runner/.blueocean-ath-jenkins-url")));
            }
            capability.setCapability("extendedDebugging", "true");
            capability.setCapability("initialBrowserUrl", launchUrl);
            if (!Strings.isNullOrEmpty(cfg.getString("TUNNEL_IDENTIFIER"))) {
                capability.setCapability("tunnelIdentifier", cfg.getString("TUNNEL_IDENTIFIER"));
            }

            WebDriver driver = new RemoteWebDriver(new URL(webDriverUrl), capability);
            LocalDriver.setCurrent(driver);
            if (cfg.getBoolean("saucelabs", false)) {
                LocalDriver.enableSauce();
                System.out.println("SauceOnDemandSessionID=" + ((RemoteWebDriver) driver).getSessionId().toString());
            }

            driver = new Augmenter().augment(driver);
            if (webDriverBrowserSize == null) {
                driver.manage().window().maximize();
            } else {
                String[] widthXHeight = webDriverBrowserSize.split("x");
                driver.manage().window().setSize(new Dimension(Integer.parseInt(widthXHeight[0]), Integer.parseInt(widthXHeight[1])));
            }
            driver.manage().deleteAllCookies();
            bind(WebDriver.class).toInstance(driver);

            bindConstant().annotatedWith(BaseUrl.class).to(launchUrl);
            LocalDriver.setUrlBase(launchUrl);

            JenkinsUser admin = new JenkinsUser(
                cfg.getString("adminUsername", "alice"),
                cfg.getString("adminPassword", "alice")
            );
            bind(JenkinsUser.class).toInstance(admin);

            CustomJenkinsServer server = new CustomJenkinsServer(new URI(launchUrl), admin);

            bind(JenkinsServer.class).toInstance(server);
            bind(CustomJenkinsServer.class).toInstance(server);

            if(server.getComputerSet().getTotalExecutors() < 10) {
                server.runScript(
                    "jenkins.model.Jenkins.getInstance().setNumExecutors(10);\n" +
                        "jenkins.model.Jenkins.getInstance().save();\n", true);
            }

            Properties properties = new Properties();
            File liveProperties = new File("live.properties");
            if (liveProperties.canRead()) {
                properties.load(new FileInputStream(liveProperties));
            }
            bind(Properties.class).annotatedWith(Names.named("live")).toInstance(properties);
        } catch (Exception e) {
            LocalDriver.destroy();
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
            .implement(RunDetailsTestsPage.class, RunDetailsTestsPage.class)
            .build(RunDetailsTestsPageFactory.class));

        install(new FactoryModuleBuilder()
            .implement(BranchPage.class, BranchPage.class)
            .build(BranchPageFactory.class));

        install(new FactoryModuleBuilder()
            .implement(PullRequestsPage.class, PullRequestsPage.class)
            .build(PullRequestsPageFactory.class));
    }
}
