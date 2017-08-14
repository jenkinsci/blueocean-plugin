package io.jenkins.blueocean.service.embedded.rest;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Simple model to hold a public key
 */
@ExportedBean
public class UserKey {
    private final String publicKey;

    public UserKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Exported(name="publickey")
    public String getPublicKey() {
        return publicKey;
    }
}
