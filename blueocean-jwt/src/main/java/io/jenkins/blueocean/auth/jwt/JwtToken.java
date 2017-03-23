package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Generates JWT token
 *
 * @author Vivek Pandey
 */
public abstract class JwtToken implements HttpResponse, ExtensionPoint{
    /**
     * JWT header
     */
    public final JSONObject header = new JSONObject();


    /**
     * JWT Claim
     */
    public final JSONObject claim = new JSONObject();

    /**
     * @return Gives HTTP header name to return generated JWT token
     */
    public abstract @Nonnull String getJwtHttpResponseHeader();

    /**
     * Generates base64 representation of JWT token sign using "RS256" algorithm
     *
     * getHeader().toBase64UrlEncode() + "." + getClaim().toBase64UrlEncode() + "." + sign
     *
     * @return base64 representation of JWT token
     */
    public abstract String sign();

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setStatus(200);
        rsp.addHeader(getJwtHttpResponseHeader(), sign());
    }

    public static @Nonnull ExtensionList<JwtToken> all(){
        return ExtensionList.lookup(JwtToken.class);
    }

    public static @CheckForNull JwtToken first(){
        for(JwtToken token:all()){
            return token;
        }
        return null;
    }
}
