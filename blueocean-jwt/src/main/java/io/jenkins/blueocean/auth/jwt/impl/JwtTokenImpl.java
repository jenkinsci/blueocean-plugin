package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import io.jenkins.blueocean.auth.jwt.JwtToken;
import io.jenkins.blueocean.auth.jwt.JwtTokenDecorator;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.security.RSADigitalSignatureConfidentialKey;
import net.sf.json.JSONObject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

/**
 * BlueOcean JWT token
 *
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public  class JwtTokenImpl extends JwtToken {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenImpl.class);

    public static final String X_BLUEOCEAN_JWT="X-BLUEOCEAN-JWT";
    private static final String DEFAULT_KEY_ID = UUID.randomUUID().toString().replace("-", "");


    @Override
    public @Nonnull String getJwtHttpResponseHeader() {
        return X_BLUEOCEAN_JWT;
    }

    /**
     * Generates base64 representation of JWT token sign using "RS256" algorithm
     *
     * getHeader().toBase64UrlEncode() + "." + getClaim().toBase64UrlEncode() + "." + sign
     *
     *
     * @return base64 representation of JWT token
     */
    @Override
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
