package io.jenkins.blueocean.ssh;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Simple model to hold a public key
 */
@ExportedBean
public class UserKey {
    private final String id;
    private final String publicKey;

    public UserKey(String id, String publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }

    @Exported(name="id")
    public String getId() {
        return id;
    }

    @Exported(name="publickey")
    public String getPublicKey() {
        return publicKey;
    }
}
