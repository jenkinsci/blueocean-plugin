package io.blueocean.ath;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.blueocean.ath.pages.classic.LoginPage;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

/**
 * ATHJUnitRunner is the JUnit runner for ATH
 *
 * It manages the WebDriver instance setup and cleanup.
 */
public class ATHJUnitRunner extends BlockJUnit4ClassRunner {
    private Injector injector;

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

    /**
     * Logs users in if the @Login annotation is used. Any other global befores for
     * Ath can be added here. next.eveulate must be the last line.
     *
     * @param method
     * @param target
     * @param statement
     * @return
     */
    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        Statement next = super.withBefores(method, target, statement);
        LoginPage loginPage = injector.getInstance(LoginPage.class);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Login methodLogin = method.getAnnotation(Login.class);
                Login classLogin = target.getClass().getAnnotation(Login.class);
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
        };
    }

    /**
     * Cleans up driver. Any other ATH per test cleanup can be done here.
     *
     * @param method
     * @param target
     * @param statement
     * @return
     */
    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        Statement next = super.withAfters(method, target, statement);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                next.evaluate();

                WebDriver driver = injector.getInstance(WebDriver.class);
                driver.close();
                driver.quit();
            }
        };
    }
}
