/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.ssh;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.IOException;

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
    private static final String BLUEOCEAN_DOMAIN_NAME = "blueocean-private-key-domain";

    /**
     * Gets the existing generated SSH key for the user or creates one and
     * returns it in the user's credential store
     * @param user owner of the key
     * @return the user's personal private key
     */
    public static @NonNull BasicSSHUserPrivateKey getOrCreate(@NonNull User user) {
        Preconditions.checkNotNull(user);

        CredentialsStore store = getUserStore(user);
        if(store == null){
            throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
        }
        // try to find the right key
        for (Credentials cred : store.getCredentials(getDomain(store))) {
            if (cred instanceof BasicSSHUserPrivateKey) {
                BasicSSHUserPrivateKey sshKey = (BasicSSHUserPrivateKey)cred;
                if (BLUEOCEAN_GENERATED_SSH_KEY_ID.equals(sshKey.getId())) {
                    return sshKey;
                }
            }
        }
        // if none found, create one
        try {
            // create one!
            String privateKey = SSHKeyUtils.generateKey(KEY_SIZE).trim();
            BasicSSHUserPrivateKey.DirectEntryPrivateKeySource keySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey);
            BasicSSHUserPrivateKey key = new BasicSSHUserPrivateKey(CredentialsScope.USER, BLUEOCEAN_GENERATED_SSH_KEY_ID, user.getId(), keySource, null, BLUEOCEAN_GENERATED_SSH_KEY_ID);
            store.addCredentials(getDomain(store), key);
            store.save();
            return key;
        } catch (IOException ex) {
            throw new ServiceException.UnexpectedErrorException("Failed to create the private key", ex);
        }
    }

    /**
     * Gets a readable SSH-compatible public key a user could paste somewhere
     * @param user the user which will be used to build a useful description
     * @param key the private key to use
     * @return a public ssh key
     */
    public static @NonNull UserKey getPublicKey(@NonNull User user, @NonNull BasicSSHUserPrivateKey key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        String publicKey = SSHKeyUtils.getPublicKey(key.getPrivateKey(), getKeyComment(user.getId())).trim();
        return new UserKey(key.getId(), publicKey);
    }

    /**
     * Resets the user's generated key by deleting it and creating a new one
     * @param user user to reset a key for
     */
    public static void reset(@NonNull User user) {
        Preconditions.checkNotNull(user);

        try {
            // create one!
            CredentialsStore store = getUserStore(user);
            if(store == null){
                throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", user.getId()));
            }

            Credentials key = null;
            // try to find the key
            for (Credentials cred : store.getCredentials(getDomain(store))) {
                if (cred instanceof BasicSSHUserPrivateKey) {
                    BasicSSHUserPrivateKey sshKey = (BasicSSHUserPrivateKey)cred;
                    if (BLUEOCEAN_GENERATED_SSH_KEY_ID.equals(sshKey.getId())) {
                        key = sshKey;
                        break;
                    }
                }
            }
            if (key != null) {
                store.removeCredentials(getDomain(store), key);
                store.save();
            }
        } catch (IOException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to reset the user's key", ex);
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
     * Returns a <user>@<jenkins-host> sort of comment to help identify the key's origin
     * @param userId a user id
     * @return an identifier
     */
    private static String getKeyComment(String userId) {
        String host = Jenkins.getInstance().getRootUrl();
        if (host == null) {
            host = Jenkins.getInstance().getRootUrlFromRequest();
        }
        host = host.replaceAll(".*//([^/]+).*", "$1");
        return ((userId == null ? Jenkins.getInstance().getDisplayName() : userId) + "@" + host)
                .replaceAll("[^:@._a-zA-Z0-9]", "");
    }

    private static Domain getDomain(CredentialsStore store) {
        Domain domain = store.getDomainByName(BLUEOCEAN_DOMAIN_NAME);
        if (domain == null) {
            try {
                //create new one
                boolean result = store.addDomain(new Domain(BLUEOCEAN_DOMAIN_NAME, null, null));
                if (!result) {
                    throw new ServiceException.UnexpectedErrorException(String.format("Failed to create credential domain: %s", BLUEOCEAN_DOMAIN_NAME));
                }
                domain = store.getDomainByName(BLUEOCEAN_DOMAIN_NAME);
                if (domain == null) {
                    throw new ServiceException.UnexpectedErrorException(String.format("Domain %s created but not found", BLUEOCEAN_DOMAIN_NAME));
                }
            } catch (IOException ex) {
                throw new ServiceException.UnexpectedErrorException("Failed to save the Blue Ocean domain.", ex);
            }
        }
        return domain;
    }
}
