package io.jenkins.blueocean.security;

import com.google.inject.Singleton;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.config.ApplicationConfig;
import org.apache.commons.codec.binary.Base64;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Singleton
public class Cookies {

    private static final Long MAX_COOKIE_AGE = TimeUnit.DAYS.toMillis(365);
    private static final String AUTH_COOKIE_NAME = "BO";

    private final CryptService crypter;

    @Inject
    public Cookies(ApplicationConfig applicationConfig) {
        crypter = new CryptService(applicationConfig.getCookiePassword(), applicationConfig.getCookieSalt());
    }

    public AuthCookieToken readAuthCookieToken(HttpServletRequest request) throws IOException {
        for(Cookie cookie: request.getCookies()) {
            if(cookie.getName().equals(AUTH_COOKIE_NAME)) {
                return AuthCookieToken.decode(crypter.cookies.decrypt(Base64.decodeBase64(cookie.getValue())));
            }
        }
        return null;
    }

    public void writeAuthCookieToken(HttpServletResponse response, Identity identity) {
        AuthCookieToken token = new AuthCookieToken(MAX_COOKIE_AGE + System.currentTimeMillis(), identity.getName());
        try {
            Cookie cookie = new Cookie(AUTH_COOKIE_NAME, Base64.encodeBase64String(crypter.cookies.crypt(token.encode())));
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("Failed to set cookie", e);
        }

    }

    public void removeAuthCookieToken(HttpServletResponse response) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}
