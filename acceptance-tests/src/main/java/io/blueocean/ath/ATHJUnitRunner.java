package io.blueocean.ath;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.blueocean.ath.pages.classic.LoginPage;
import org.apache.log4j.Logger;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.ScreenshotException;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * ATHJUnitRunner is the JUnit runner for ATH
 *
 * It manages the WebDriver instance setup and cleanup.
 */
public class ATHJUnitRunner extends BlockJUnit4ClassRunner {
    private Injector injector;

    private Logger logger = Logger.getLogger(ATHJUnitRunner.class);

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
                Login classLogin = test.getClass().getAnnotation(Login.class);

                if(methodLogin != null ) {
                    if(!methodLogin.disable()) {
                        loginPage.login();
                    }
                } else if(classLogin != null) {
                    if(!classLogin.disable()){
                        loginPage.login();
                    }
                }

                try {
                    next.evaluate();
                } catch (Exception e) {
                    writeScreenShotCause(e, test, method);
                    throw e;

                }


                WebDriver driver = injector.getInstance(WebDriver.class);
                driver.close();
                driver.quit();
            }
        };
    }

    private void writeScreenShotCause(Throwable t, Object test, FrameworkMethod method) throws IOException {
        Throwable cause = t.getCause();
        while(cause != null) {
            if(cause instanceof ScreenshotException) {
                ScreenshotException se = ((ScreenshotException) cause);

                byte[] screenshot =  Base64.getMimeDecoder().decode(se.getBase64EncodedScreenshot());


                File file = new File("target/screenshots/"+ test.getClass().getName() + "_" + method.getName() + ".png");
                Files.createParentDirs(file);
                Files.write(screenshot, file);
                logger.info("Wrote screenshot to " + file.getAbsolutePath());

                break;
            } else {

                cause = cause.getCause();
            }

        }
     }
}
