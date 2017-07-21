package io.jenkins.blueocean.credential;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.base.Preconditions;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Helper to deal with managing a Jenkins-created per-user SSH key
 * @author kzantow
 */
@Restricted(NoExternalUse.class)
public class UserSSHKeyManager {
    private static final int KEY_SIZE = 2048;
    private static final String BLUEOCEAN_GENERATED_SSH_KEY_ID = "jenkins-generated-ssh-key";
    private static final String BLUEOCEAN_DOMAIN_NAME = "blueocean-git-domain";
    private static final Domain BLUEOCEAN_DOMAIN = new Domain(BLUEOCEAN_DOMAIN_NAME, null, null);
    
    /**
     * Gets the existing generated SSH key for the user or creates one and
     * returns it in the user's credential store
     * @param user owner of the key
     * @return the user's personal private key
     */
    public static @Nonnull BasicSSHUserPrivateKey getOrCreate(@Nonnull User user) {
        Preconditions.checkNotNull(user);
        
        CredentialsStore store = getUserStore(user);
        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }
        // try to find the right key
        for (Credentials cred : store.getCredentials(BLUEOCEAN_DOMAIN)) {
            if (cred instanceof BasicSSHUserPrivateKey) {
                BasicSSHUserPrivateKey sshKey = (BasicSSHUserPrivateKey)cred;
                if (BLUEOCEAN_GENERATED_SSH_KEY_ID.equals(sshKey.getId())) {
                    return sshKey;
                }
            }
        }
        // if none found, create one
        BasicSSHUserPrivateKey key;
        try {
            // create one!
            KeyPair keyPair = SSHKeyUtils.generateRSAKey(KEY_SIZE);
            RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
            String id_rsa = Base64.encode(privateKey.getEncoded());
            BasicSSHUserPrivateKey.DirectEntryPrivateKeySource keySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(id_rsa);
            key = new BasicSSHUserPrivateKey(CredentialsScope.USER, BLUEOCEAN_GENERATED_SSH_KEY_ID, user.getId(), keySource, null, BLUEOCEAN_GENERATED_SSH_KEY_ID);
            store.addCredentials(BLUEOCEAN_DOMAIN, key);
            store.save();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return key;
    }
    
    /**
     * Gets a readable SSH-compatible public key a user could paste somewhere
     * @param user
     * @param key 
     * @return 
     */
    public static @Nonnull String getReadablePublicKey(@Nonnull User user, @Nonnull BasicSSHUserPrivateKey key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);
        
        PKCS8EncodedKeySpec keySpec =
            new PKCS8EncodedKeySpec(Base64.decode(key.getPrivateKey()));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey)keyFactory.generatePrivate(keySpec);
            RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent());
            RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(publicKeySpec);
            String id_rsa_pub = getSSHPublicKey(publicKey, user);
            return id_rsa_pub;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Resets the user's generated key by deleting it and creating a new one
     * @param user 
     */
    public static void reset(@Nonnull User user) {
        Preconditions.checkNotNull(user);
        
        try {
            // create one!
            CredentialsStore store = getUserStore(user);
            if(store == null){
                throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
            }
            
            Credentials key = null;
            // try to find the key
            for (Credentials cred : store.getCredentials(BLUEOCEAN_DOMAIN)) {
                if (cred instanceof BasicSSHUserPrivateKey) {
                    BasicSSHUserPrivateKey sshKey = (BasicSSHUserPrivateKey)cred;
                    if (BLUEOCEAN_GENERATED_SSH_KEY_ID.equals(sshKey.getId())) {
                        key = sshKey;
                        break;
                    }
                }
            }
            if (key != null) {
                store.removeCredentials(BLUEOCEAN_DOMAIN, key);
                store.save();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the user's CredentialStore
     * @param user user to find a store for
     * @return the credential store or null if not found
     */
    private static @CheckForNull CredentialsStore getUserStore(User user){
        for(CredentialsStore s : CredentialsProvider.lookupStores(user)) {
            if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                return s;
            }
        }
        return null;
    }
    
    /**
     * Gets an "ssh-rsa"-style formatted public key with useful identifier
     */
    private static String getSSHPublicKey(RSAPublicKey key, User user) throws IOException {
        return "ssh-rsa " + Base64.encode(SSHKeyUtils.encodePublicKey(key)) + " " + getKeyComment(user.getId());
    }

    /**
     * Returns a <user>@<jenkins> sort of comment to help identify the key's origin
     * @param userId a user id
     * @return an identifier
     */
    private static String getKeyComment(String userId) {
        return ((userId == null ? "user" : userId) + "@"
            + Jenkins.getInstance().getDisplayName())
                .replaceAll("[^@._a-zA-Z0-9]", "");
    }
}
