package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import io.jenkins.blueocean.auth.jwt.JwtToken;
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

/**
 * BlueOcean JWT token
 *
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public  class JwtTokenImpl extends JwtToken {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenImpl.class);

    public static final String X_BLUEOCEAN_JWT="X-BLUEOCEAN-JWT";

    @Override
    public @Nonnull String getJwtHttpResponseHeader() {
        return X_BLUEOCEAN_JWT;
    }

    @Override
    public String sign(String keyId){
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
