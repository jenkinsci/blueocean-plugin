package io.jenkins.blueocean.auth.jwt.impl;

import io.jenkins.blueocean.auth.jwt.SigningKey;
import jenkins.security.RSADigitalSignatureConfidentialKey;

import java.io.IOException;

/**
 * Safely store RSA key pair used to sign JWT tokens.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public final class JwtRsaDigitalSignatureKey extends RSADigitalSignatureConfidentialKey {
    private final String id;

    public JwtRsaDigitalSignatureKey(String id) {
        super("blueoceanJwt-" + id);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public SigningKey toSigningKey() {
        return new SigningKey(id,getPrivateKey());
    }

    public boolean exists() throws IOException {
        return super.load() != null;
    }
}
