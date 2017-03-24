package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.CheckForNull;
import java.security.interfaces.RSAPublicKey;

/**
 * PublicKey provider, to be used during signing
 *
 * @author Vivek Pandey
 * @see JwtTokenVerifier
 */
public abstract class JwtPublicKeyProvider implements ExtensionPoint{
    /**
     * Provides public key needed to verify the token using claims.
     *
     * @param keyId JWT token keyId to get public key
     */
    public abstract @CheckForNull RSAPublicKey getPublicKey(String keyId);

    public static ExtensionList<JwtPublicKeyProvider> all(){
        return  ExtensionList.lookup(JwtPublicKeyProvider.class);
    }

    public @CheckForNull static JwtPublicKeyProvider first(String keyId){
        for(JwtPublicKeyProvider provider:all()){
            if(provider.getPublicKey(keyId) != null) {
                return provider;
            }
        }
        return null;
    }
}
