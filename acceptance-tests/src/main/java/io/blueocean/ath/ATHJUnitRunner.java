package io.blueocean.ath;

import com.google.inject.Injector;
import io.blueocean.ath.pages.classic.LoginPage;
import org.jukito.JukitoRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;

public class ATHJUnitRunner extends JukitoRunner {


    public ATHJUnitRunner(Class<?> klass) throws InitializationError, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(klass);
    }

    public ATHJUnitRunner(Class<?> klass, Injector injector) throws InitializationError, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(klass, injector);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement statement = super.methodInvoker(method, test);
        statement = new LoginBefore(getInjector().getInstance(LoginPage.class), method, test, statement);
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
