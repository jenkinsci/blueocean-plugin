package io.jenkins.blueocean.auth.jwt;

import java.security.interfaces.RSAPrivateKey;

/**
 * Key used to sign JWT token.
 *
 * @author Kohsuke Kawaguchi
 * @see SigningPublicKey
 * @see JwtSigningKeyProvider
 */
public final class SigningKey {
    /**
     * Identifier of this key, used later to obtain the public key that verifies the signature.
     *
     * @see JwtSigningKeyProvider#getPublicKey(String)
     */
    private final String kid;
    /**
     * Private key used to sign the token
     */
    private final RSAPrivateKey key;

    public SigningKey(String kid, RSAPrivateKey key) {
        this.kid = kid;
        this.key = key;
    }

    public String getKid() {
        return kid;
    }

    public RSAPrivateKey getKey() {
        return key;
    }
}
