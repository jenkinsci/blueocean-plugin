package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.jenkins.plugins.bitbucket.client.JwtCredentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.blueocean.commons.ServiceException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import javax.annotation.Nonnull;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * @author Vivek Pandey
 */
public class BbConnectCredential extends JwtCredentials {
    private final String username;
    private final Secret password;
    private final Secret clientKey;
    private final String teamName;
    private final String teamDisplayName;

    public BbConnectCredential(@Nonnull CredentialsScope scope,
                               @Nonnull String id,
                               @Nonnull String description,
                               @Nonnull String username,
                               @Nonnull String teamName,
                               @Nonnull String teamDisplayName,
                               @Nonnull String password,
                               @Nonnull String clientKey) {
        super(scope, id, description);
        this.username = username;
        this.password = Secret.fromString(password);
        this.clientKey = Secret.fromString(clientKey);
        this.teamDisplayName = teamDisplayName;
        this.teamName = teamName;
    }

    @Nonnull
    @Override
    public String getUsername() {
        return username;
    }

    @Nonnull
    @Override
    public Secret getPassword() {
        return password;
    }

    public Secret getClientKey() {
        return clientKey;
    }

    public String getTeamDisplayName() {
        return teamDisplayName;
    }

    @Override
    public String getAuthenticationHeaderScheme() {
        return "JWT";
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    @Override
    public String getJwtToken() {
        return getJwtToken(password.getPlainText(), clientKey.getPlainText());
    }

    static @Nonnull String getJwtAuthHeader(@Nonnull String secret, @Nonnull String clientKey){
        return "JWT " + getJwtToken(secret, clientKey);
    }
    static @Nonnull String getJwtToken(@Nonnull String secret, @Nonnull String clientKey){
        long issuedAt = System.currentTimeMillis() / 1000L;
        long expiresAt = issuedAt + 180L;
        String key = BitbucketCloudConnect.BB_ADDON_KEY;
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(key);
            claims.setSubject(clientKey);
            claims.setExpirationTime(NumericDate.fromSeconds(expiresAt));
            claims.setIssuedAt(NumericDate.fromSeconds(issuedAt));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());

            jws.setKey(new SecretKeySpec(secret.getBytes("UTF-8"), "HMACSHA256"));

            // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

            return jws.getCompactSerialization();
        } catch (UnsupportedEncodingException |JoseException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    @Override
    public Secret clientKey() {
        return clientKey;
    }

    @Override
    public Secret sharedSecret() {
        return password;
    }

    @Extension(ordinal = 10.0D)
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        public String getDisplayName() {
            return "JWT Credentials";
        }

        public String getIconClassName() {
            return "icon-credentials-userpass";
        }
    }
}
