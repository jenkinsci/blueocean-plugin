package io.jenkins.blueocean.auth.jwt;

import com.google.common.collect.ImmutableList;
import hudson.remoting.Base64;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.WebMethod;

import java.security.interfaces.RSAPublicKey;

/**
 * Public key counterpart of {@link SigningKey}
 *
 * @author Kohsuke Kawaguchi
 */
public class SigningPublicKey {
    /**
     * Identifier of this key, used later to obtain the public key that verifies the signature.
     *
     * @see JwtSigningKeyProvider#getPublicKey(String)
     */
    private final String kid;
    /**
     * Private key used to sign the token
     */
    private final RSAPublicKey key;

    public SigningPublicKey(String kid, RSAPublicKey key) {
        this.kid = kid;
        this.key = key;
    }

    public String getKid() {
        return kid;
    }

    public RSAPublicKey getKey() {
        return key;
    }

    /**
     * Renders the key as JSON in the JWK format.
     */
    @WebMethod(name = "")
    public JSONObject asJSON() {
        JSONObject jwk = new JSONObject();
        jwk.put("kty", "RSA");
        jwk.put("alg","RS256");
        jwk.put("kid",kid);
        jwk.put("use", "sig");
        jwk.put("key_ops", ImmutableList.of("verify"));
        jwk.put("n", Base64.encode(key.getModulus().toByteArray()));
        jwk.put("e", Base64.encode(key.getPublicExponent().toByteArray()));
        return jwk;
    }
}
