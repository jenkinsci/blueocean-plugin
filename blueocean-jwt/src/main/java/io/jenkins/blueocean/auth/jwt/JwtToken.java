package io.jenkins.blueocean.auth.jwt;

import io.jenkins.blueocean.commons.ServiceException;
import jenkins.security.RSADigitalSignatureConfidentialKey;
import net.sf.json.JSONObject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

/**
 * JWT token
 *
 * Generates JWT token
 *
 * @author Vivek Pandey
 */
public final class JwtToken  implements HttpResponse{
    private static final Logger logger = LoggerFactory.getLogger(JwtToken.class);

    public static final String X_BLUEOCEAN_JWT="X-BLUEOCEAN-JWT";
    private static final String DEFAULT_KEY_ID = UUID.randomUUID().toString().replace("-", "");

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
     *
     * @return base64 representation of JWT token
     */
    public String sign(){
        for(JwtTokenDecorator decorator: JwtTokenDecorator.all()){
            decorator.decorate(this);
        }

        /**
         *  kid might have been set already by using {@link #header} or {@link JwtTokenDecorator}, if present use it
         *  otherwise use the default kid
         */
        String keyId = (String)header.get(HeaderParameterNames.KEY_ID);
        if(keyId == null){
            keyId = DEFAULT_KEY_ID;
        }

        JwtRsaDigitalSignatureKey rsaDigitalSignatureConfidentialKey = new JwtRsaDigitalSignatureKey(keyId);

        try {
            return rsaDigitalSignatureConfidentialKey.sign(claim);
        } catch (JoseException e) {
            String msg = "Failed to sign JWT token: "+e.getMessage();
            logger.error(msg);
            throw new ServiceException.UnexpectedErrorException(msg, e);
        }
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setStatus(200);
        rsp.addHeader(X_BLUEOCEAN_JWT, sign());
    }

    public final static class JwtRsaDigitalSignatureKey extends RSADigitalSignatureConfidentialKey{
        private final String id;

        public JwtRsaDigitalSignatureKey(String id) {
            super("blueoceanJwt-"+id);
            this.id = id;
        }

        public String sign(JSONObject claim) throws JoseException {
            JsonWebSignature jsonWebSignature = new JsonWebSignature();
            jsonWebSignature.setPayload(claim.toString());
            jsonWebSignature.setKey(getPrivateKey());
            jsonWebSignature.setKeyIdHeaderValue(id);
            jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            jsonWebSignature.setHeader(HeaderParameterNames.TYPE, "JWT");

            return jsonWebSignature.getCompactSerialization();
        }

        public boolean exists() throws IOException {
            return super.load()!=null;
        }
    }
}
