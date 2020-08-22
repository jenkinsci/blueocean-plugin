package io.jenkins.blueocean.auth.jwt.impl;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.util.Cookie;
import hudson.model.User;
import hudson.tasks.Mailer;
import net.sf.json.JSONObject;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class JwtAuthenticationServiceImplTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void getToken() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        User user = User.get("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        JenkinsRule.WebClient webClient = j.createWebClient();

        webClient.login("alice");

        String token = getToken(webClient);

        Assert.assertNotNull(token);

        JsonWebStructure jsonWebStructure = JsonWebStructure.fromCompactSerialization(token);

        Assert.assertTrue(jsonWebStructure instanceof JsonWebSignature);

        JsonWebSignature jsw = (JsonWebSignature) jsonWebStructure;

        System.out.println(token);
        System.out.println(jsw.toString());


        String kid = jsw.getHeader("kid");

        Assert.assertNotNull(kid);

        Page page = webClient.goTo("jwt-auth/jwks/"+kid+"/", "application/json");

//        for(NameValuePair valuePair: page.getWebResponse().getResponseHeaders()){
//            System.out.println(valuePair);
//        }

        JSONObject jsonObject = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        System.out.println(jsonObject.toString());
        RsaJsonWebKey rsaJsonWebKey = new RsaJsonWebKey(jsonObject,null);

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setRequireExpirationTime() // the JWT must have an expiration time
            .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
            .setRequireSubject() // the JWT must have a subject claim
            .setVerificationKey(rsaJsonWebKey.getKey()) // verify the sign with the public key
            .build(); // create the JwtConsumer instance

        JwtClaims claims = jwtConsumer.processToClaims(token);
        Assert.assertEquals("alice",claims.getSubject());

        Map<String,Object> claimMap = claims.getClaimsMap();

        Map<String,Object> context = (Map<String, Object>) claimMap.get("context");
        Map<String,String> userContext = (Map<String, String>) context.get("user");
        Assert.assertEquals("alice", userContext.get("id"));
        Assert.assertEquals("Alice Cooper", userContext.get("fullName"));
        Assert.assertEquals("alice@jenkins-ci.org", userContext.get("email"));
    }

    @Test
    public void anonymousUserToken() throws Exception{
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        JenkinsRule.WebClient webClient = j.createWebClient();
        String token = getToken(webClient);
        Assert.assertNotNull(token);


        JsonWebStructure jsonWebStructure = JsonWebStructure.fromCompactSerialization(token);

        Assert.assertTrue(jsonWebStructure instanceof JsonWebSignature);

        JsonWebSignature jsw = (JsonWebSignature) jsonWebStructure;


        String kid = jsw.getHeader("kid");

        Assert.assertNotNull(kid);

        Page page = webClient.goTo("jwt-auth/jwks/"+kid+"/", "application/json");

//        for(NameValuePair valuePair: page.getWebResponse().getResponseHeaders()){
//            System.out.println(valuePair);
//        }

        JSONObject jsonObject = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        RsaJsonWebKey rsaJsonWebKey = new RsaJsonWebKey(jsonObject,null);

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setRequireExpirationTime() // the JWT must have an expiration time
            .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
            .setRequireSubject() // the JWT must have a subject claim
            .setVerificationKey(rsaJsonWebKey.getKey()) // verify the sign with the public key
            .build(); // create the JwtConsumer instance

        JwtClaims claims = jwtConsumer.processToClaims(token);
        Assert.assertEquals("anonymous",claims.getSubject());

        Map<String,Object> claimMap = claims.getClaimsMap();

        Map<String,Object> context = (Map<String, Object>) claimMap.get("context");
        Map<String,String> userContext = (Map<String, String>) context.get("user");
        Assert.assertEquals("anonymous", userContext.get("id"));
    }

    // webclient has problems with pages returning 204, so we use HttpURLConnection directly to handle the token
    private String getToken(JenkinsRule.WebClient webClient) throws IOException {
        URL tokenUrl = new URL(webClient.getContextPath() + "jwt-auth/token/");
        HttpURLConnection  connection = (HttpURLConnection) tokenUrl.openConnection();
        Set<Cookie> cookies = webClient.getCookies(tokenUrl);
        for (Cookie cookie : cookies) {
            connection.addRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
        Assert.assertEquals(connection.getResponseCode(), 204);
        String token = connection.getHeaderField("X-BLUEOCEAN-JWT");
        connection.disconnect();
        return token;
    }

    @Test
    public void getJwks() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        JenkinsRule.WebClient webClient = j.createWebClient();

        User user = User.get("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        webClient.login("alice");
        String token = getToken(webClient); // this call triggers the creation of a RSA key in RSAConfidentialKey::getPrivateKey

        String jwksPayload = webClient.goTo("jwt-auth/jwk-set", "application/json").getWebResponse().getContentAsString();
        System.out.println(jwksPayload);
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwksPayload);
        JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(jsonWebKeySet.getJsonWebKeys());

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setRequireExpirationTime() // the JWT must have an expiration time
            .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
            .setRequireSubject() // the JWT must have a subject claim
            .setVerificationKeyResolver(jwksResolver) // verify the sign with the public key
            .build(); // create the JwtConsumer instance

        JwtClaims claims = jwtConsumer.processToClaims(token);
        Assert.assertEquals("alice", claims.getSubject());

        Map<String,Object> claimMap = claims.getClaimsMap();
        Map<String,Object> context = (Map<String, Object>) claimMap.get("context");
        Map<String,String> userContext = (Map<String, String>) context.get("user");
        Assert.assertEquals("alice", userContext.get("id"));
        Assert.assertEquals("Alice Cooper", userContext.get("fullName"));
        Assert.assertEquals("alice@jenkins-ci.org", userContext.get("email"));
    }
}
