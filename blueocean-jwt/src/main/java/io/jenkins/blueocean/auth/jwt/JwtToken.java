package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.commons.ServiceException;
import net.sf.json.JSONObject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates JWT token
 *
 * @author Vivek Pandey
 */
public class JwtToken implements HttpResponse {
    private static final Logger LOGGER = Logger.getLogger(JwtToken.class.getName());

    /**
     * {@link JwtToken} is sent as HTTP header of name.
     */
    public static final String X_BLUEOCEAN_JWT="X-BLUEOCEAN-JWT";

    /**
     * JWT header
     */
    public final JSONObject header = new JSONObject();


    /**
     * JWT Claim
     */
    public final JSONObject claim = new JSONObject();

    /**
     * Generates base64 representation of JWT token sign using "RS256" algorithm
     *
     * getHeader().toBase64UrlEncode() + "." + getClaim().toBase64UrlEncode() + "." + sign
     *
     * @return base64 representation of JWT token
     */
    public String sign() {
        for(JwtTokenDecorator decorator: JwtTokenDecorator.all()){
            decorator.decorate(this);
        }

        for(JwtSigningKeyProvider signer: JwtSigningKeyProvider.all()){
            SigningKey k = signer.select(this);
            if (k!=null) {
                try {
                    JsonWebSignature jsonWebSignature = new JsonWebSignature();
                    jsonWebSignature.setPayload(claim.toString());
                    jsonWebSignature.setKey(k.getKey());
                    jsonWebSignature.setKeyIdHeaderValue(k.getKid());
                    jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
                    jsonWebSignature.setHeader(HeaderParameterNames.TYPE, "JWT");

                    return jsonWebSignature.getCompactSerialization();
                } catch (JoseException e) {
                    String msg = "Failed to sign JWT token: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, "Failed to sign JWT token", e);
                    throw new ServiceException.UnexpectedErrorException(msg, e);
                }
            }
        }

        throw new IllegalStateException("No key is available to sign a token");
    }

    /**
     * Writes the token as an HTTP response.
     */
    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setStatus(200);
        rsp.addHeader(X_BLUEOCEAN_JWT, sign());
    }
}
