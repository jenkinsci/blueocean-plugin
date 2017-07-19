package io.jenkins.blueocean.credential;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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

/**
 * Helper to deal with managing a Jenkins-created per-user SSH key
 * @author kzantow
 */
public class SSHCredentialManager {
    public static final int KEY_SIZE = 2048;
    public static final String BLUEOCEAN_GENERATED_SSH_KEY_ID = "jenkins-generated-ssh-key";
    public static final String BLUEOCEAN_DOMAIN_NAME = "blueocean-git-domain";
    public static final String BLUEOCEAN_GENERATED_SSH_KEY_DOMAIN = "blueocean-generated-ssh-key";
    public static final BlueOceanDomainSpecification BLUEOCEAN_DOMAIN_SPECIFICATION = new BlueOceanDomainSpecification();
    public static final BlueOceanDomainRequirement BLUEOCEAN_DOMAIN_REQUIREMENT = new BlueOceanDomainRequirement();
    
    /**
     * Gets the existing generated SSH key for the user or creates one and
     * returns it in the user's credential store
     * @param user owner of the key
     * @return the user's personal private key
     */
    public BasicSSHUserPrivateKey getOrCreatePersonalCredential(@Nonnull User user) {
        List<BasicSSHUserPrivateKey> credentials = CredentialsProvider.lookupCredentials(BasicSSHUserPrivateKey.class, Jenkins.getInstance().getItemGroup(), user.impersonate(), BLUEOCEAN_DOMAIN_REQUIREMENT);
        // try to find the right key
        for (BasicSSHUserPrivateKey sshKey : credentials) {
            if (BLUEOCEAN_GENERATED_SSH_KEY_ID.equals(sshKey.getId())) {
                return sshKey;
            }
        }
        // if none found, create one
        BasicSSHUserPrivateKey key;
        try {
            // create one!
            CredentialsStore store = getUserStore(user);
            if(store == null){
                throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
            }
            KeyPair keyPair = generateRSAKey(KEY_SIZE);
            RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
            String id_rsa = Base64.encode(privateKey.getEncoded());
            BasicSSHUserPrivateKey.DirectEntryPrivateKeySource keySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(id_rsa);
            key = new BasicSSHUserPrivateKey(CredentialsScope.USER, BLUEOCEAN_GENERATED_SSH_KEY_ID, user.getId(), keySource, null, BLUEOCEAN_GENERATED_SSH_KEY_ID);
            Domain domain = getBlueOceanDomain();
            store.addCredentials(domain, key);
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
    public String getReadablePublicKey(@Nonnull User user, BasicSSHUserPrivateKey key) {
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
    private String getSSHPublicKey(RSAPublicKey key, User user) throws IOException {
        return "ssh-rsa " + Base64.encode(encodePublicKey(key)) + " " + getKeyComment(user.getId());
    }

    /**
     * Returns a <user>@<jenkins> sort of comment to help identify the key's origin
     * @param userId a user id
     * @return an identifier
     */
    private String getKeyComment(String userId) {
        return ((userId == null ? "user" : userId) + "@"
            + Jenkins.getInstance().getDisplayName())
                .replaceAll("[^@._a-zA-Z0-9]", "");
    }
    
    /**
     * Generates a new RSA key with specified keySize
     * @return a public/private key pair
     */
    private static KeyPair generateRSAKey(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Encodes the public key according to some spec somewhere
     * @param key public key to use
     * @return the ssh-rsa bytes
     * @throws IOException 
     */
    private static byte[] encodePublicKey(RSAPublicKey key) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /* encode the "ssh-rsa" string */
        byte[] sshrsa = new byte[] { 0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a' };
        out.write(sshrsa);
        /* Encode the public exponent */
        BigInteger e = key.getPublicExponent();
        byte[] data = e.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        /* Encode the modulus */
        BigInteger m = key.getModulus();
        data = m.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        return out.toByteArray();
    }

    private static void encodeUInt32(int value, OutputStream out) throws IOException {
        byte[] tmp = new byte[4];
        tmp[0] = (byte) ((value >>> 24) & 0xff);
        tmp[1] = (byte) ((value >>> 16) & 0xff);
        tmp[2] = (byte) ((value >>> 8) & 0xff);
        tmp[3] = (byte) (value & 0xff);
        out.write(tmp);
    }

    /**
     * Provides the domain we use to store the credential
     * @return a domain
     */
    @Nonnull
    private Domain getBlueOceanDomain() {
        return new Domain(BLUEOCEAN_DOMAIN_NAME, null, null);
    }
}
