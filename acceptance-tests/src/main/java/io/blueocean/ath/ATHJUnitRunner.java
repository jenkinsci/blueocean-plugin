package io.blueocean.ath;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.blueocean.ath.pages.classic.LoginPage;
import org.apache.commons.io.FileUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * ATHJUnitRunner is the JUnit runner for ATH
 *
 * It manages the WebDriver instance setup and cleanup.
 */
public class ATHJUnitRunner extends BlockJUnit4ClassRunner {
    private Injector injector;

    private Logger logger = LoggerFactory.getLogger(ATHJUnitRunner.class);

    public ATHJUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * Create the Test class. A new injector is created every time to get a fresh
     * copy of WebDriver
     *
     * @return
     * @throws Exception
     */
    @Override
    public Object createTest() throws Exception {
        injector = Guice.createInjector(new AthModule());
        return injector.getInstance(super.getTestClass().getJavaClass());
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement next =  super.methodInvoker(method, test);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LoginPage loginPage = injector.getInstance(LoginPage.class);

                Login methodLogin = method.getAnnotation(Login.class);

                // determine whether test should login first or not
                // first check test method, then walk up hierarchy at class level only
                if(methodLogin != null ) {
                    if(!methodLogin.disable()) {
                        loginPage.login();
                    }
                } else {
                    Class<?> clazz = test.getClass();

                    while (clazz != null) {
                        Login classLogin = clazz.getAnnotation(Login.class);

                        if (classLogin != null) {
                            if (!classLogin.disable()) {
                                loginPage.login();
                            }
                            break;
                        }

                        clazz = clazz.getSuperclass();
                    }
                }

                try {
                    if (LocalDriver.isSauceLabsMode()) {
                        logger.info("SauceOnDemandSessionID=" + ((RemoteWebDriver) LocalDriver.getDriver()).getSessionId().toString());
                    }

                    next.evaluate();
                    outputConsoleLogs();
                    LocalDriver.executeSauce("job-result=passed");
                } catch (Exception e) {
                    LocalDriver.executeSauce("job-result=failed");
                    writeScreenShotCause(e, test, method);
                    outputConsoleLogs();
                    throw e;
                }

                WebDriver driver = injector.getInstance(WebDriver.class);
                driver.close();
                driver.quit();
            }
        };
    }

    private void writeScreenShotCause(Throwable t, Object test, FrameworkMethod method) throws IOException {
        WebDriver driver = injector.getInstance(WebDriver.class);
        File file = new File("target/screenshots/"+ test.getClass().getName() + "_" + method.getName() + ".png");

        Throwable cause = t.getCause();
        boolean fromException = false;
        while(cause != null) {
            if(cause instanceof ScreenshotException) {
                ScreenshotException se = ((ScreenshotException) cause);

                byte[] screenshot =  Base64.getMimeDecoder().decode(se.getBase64EncodedScreenshot());

                Files.createParentDirs(file);
                Files.write(screenshot, file);
                logger.info("Wrote screenshot to " + file.getAbsolutePath());
                fromException = true;
                break;
            } else {
                cause = cause.getCause();
            }
        }

        if(!fromException) {
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, file);
            logger.info("Wrote screenshot to " + file.getAbsolutePath());
        }
    }

    private void outputConsoleLogs() {
        WebDriver driver = injector.getInstance(WebDriver.class);
        List<LogEntry> allEntries = driver.manage().logs().get("browser").getAll();
        LogEntryLogger.recordLogEntries(allEntries);
    }


    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
            runTest(methodBlock(method), description, notifier, method.getAnnotation(Retry.class));
        }
    }

    private void runTest(Statement statement, Description description,
                                 RunNotifier notifier, Retry retry) {
        logger.info(String.format("Running test: '%s'", description.getMethodName()));
        if (LocalDriver.isSauceLabsMode()) {
            logger.info("SauceOnDemandSessionID=" + ((RemoteWebDriver) LocalDriver.getDriver()).getSessionId().toString());
        }
        String buildName = System.getenv("BUILD_TAG");
        if (buildName == null || buildName == "") {
            buildName = System.getenv("SAUCE_BUILD_NAME");
        }
        if (buildName == null || buildName == "") {
            buildName = System.getenv("BUILD_TAG");
        }
        // https://wiki.saucelabs.com/display/DOCS/Annotating+Tests+with+Selenium%27s+JavaScript+Executor
        if (buildName != null && buildName != "") {
            LocalDriver.executeSauce(String.format("job-build=%s", buildName));
        }
        LocalDriver.executeSauce(String.format("job-name=%s", description.getMethodName()));

        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        List<Throwable> failures = Lists.newArrayList();
        try {
            int n = retry == null ? 1 : retry.value();

            for (int i = 0; i < n; i++) {
                try {
                    statement.evaluate();
                    failures.clear();
                    break;
                } catch (AssumptionViolatedException e) {
                    throw e;
                } catch (Throwable e) {
                    if(n <= 1) {
                        failures.add(e);
                    } else {
                        failures.add(new RetryThrowable(i, e));
                    }
                }
            }

            for (Throwable failure : failures) {
                eachNotifier.addFailure(failure);
            }
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
            LocalDriver.destroy();
        }
    }

    public class RetryThrowable extends Throwable {
        public RetryThrowable(int n, Throwable cause) {
            super("Retry " + n, cause);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
