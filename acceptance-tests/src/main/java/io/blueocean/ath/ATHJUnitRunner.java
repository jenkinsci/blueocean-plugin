package io.blueocean.ath;

import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.blueocean.ath.pages.classic.LoginPage;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ATHJUnitRunner extends BlockJUnit4ClassRunner {
    private Injector injector;
    private WebDriver driver;
    @Override
    public Object createTest() throws Exception {
        Object obj = super.createTest();
        injector.injectMembers(obj);
        return obj;
    }

    public ATHJUnitRunner(Class<?> klass) throws InitializationError, IOException {
        super(klass);
        injector = createInjector();
    }

    private Injector createInjector() throws InitializationError, IOException {
        DesiredCapabilities capability = DesiredCapabilities.firefox();
        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();
        String launchUrl = new String(Files.readAllBytes(Paths.get("runner/.blueocean-ath-jenkins-url")));

        return Guice.createInjector(new AthModule(driver, launchUrl));
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        driver.close();
        driver.quit();
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement statement = super.methodInvoker(method, test);
        statement = new LoginBefore(injector.getInstance(LoginPage.class), method, test, statement);
        return statement;
    }

    static class LoginBefore extends Statement {
        private LoginPage loginPage;
        private FrameworkMethod method;
        private Object test;
        private Statement next;

        public LoginBefore(LoginPage loginPage, FrameworkMethod method, Object test, Statement next) {
            this.loginPage = loginPage;
            this.method = method;
            this.test = test;
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
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

            next.evaluate();
        }
    }
}
