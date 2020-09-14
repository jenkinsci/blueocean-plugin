package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.CheckForNull;

/**
 * PublicKey provider, to be used during signing
 *
 * @author Vivek Pandey
 * @see JwtTokenVerifier
 */
public abstract class JwtSigningKeyProvider implements ExtensionPoint {
    /**
     * Chooses the key to sign the given token.
     *
     * @param token
     *      Token to be signed
     * @return
     *      null if this provider refuses to sign the given token, in which case the next provider will get the chance.
     */
    public abstract @CheckForNull SigningKey select(JwtToken token);

    /**
     * Provides public key needed to verify the token.
     *
     * @param keyId
     *      {@link SigningKey#kid} returned from {@link #select(JwtToken)}
     * @return
     *      null if this provider doesn't recognize the given key ID.
     */
    public abstract @CheckForNull SigningPublicKey getPublicKey(String keyId);

    public static ExtensionList<JwtSigningKeyProvider> all(){
        return  ExtensionList.lookup(JwtSigningKeyProvider.class);
    }

    /**
     * Search through all the providers and find the public key that matches the given key ID.
     */
    public @CheckForNull static SigningPublicKey toPublicKey(String keyId){
        for(JwtSigningKeyProvider provider:all()){
            SigningPublicKey key = provider.getPublicKey(keyId);
            if(key != null) {
                return key;
            }
        }
        return null;
    }
}
