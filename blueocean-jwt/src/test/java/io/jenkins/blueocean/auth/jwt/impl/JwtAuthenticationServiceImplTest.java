package io.jenkins.blueocean.auth.jwt.impl;

import com.gargoylesoftware.htmlunit.Page;
import hudson.model.User;
import hudson.tasks.Mailer;
import net.sf.json.JSONObject;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.util.security.Password;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class JwtAuthenticationServiceImplTest {

    @Rule
    public JenkinsRule j = new JenkinsRule(){
        @Override
        protected LoginService configureUserRealm() {
            HashLoginService realm = new HashLoginService();
            realm.setName("default");   // this is the magic realm name to make it effective on everywhere
            realm.update("alice", new Password("alice"), new String[]{"user","female"});
            realm.update("bob", new Password("bob"), new String[]{"user","male"});
            realm.update("charlie", new Password("charlie"), new String[]{"user","male"});
            return realm;
        }
    };

    @Test
    public void getToken() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        User user = User.get("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        JenkinsRule.WebClient webClient = j.createWebClient();

        webClient.login("alice");

        Page page = webClient.goTo("jwt-auth/token/", null);
        String token = page.getWebResponse().getResponseHeaderValue("X-BLUEOCEAN-JWT");

        Assert.assertNotNull(token);

        JsonWebStructure jsonWebStructure = JsonWebStructure.fromCompactSerialization(token);

        Assert.assertTrue(jsonWebStructure instanceof JsonWebSignature);

        JsonWebSignature jsw = (JsonWebSignature) jsonWebStructure;

        System.out.println(token);
        System.out.println(jsw.toString());


        String kid = jsw.getHeader("kid");

        Assert.assertNotNull(kid);

        page = webClient.goTo("jwt-auth/jwks/"+kid+"/", "application/json");

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
        Page page = webClient.goTo("jwt-auth/token/", null);
        String token = page.getWebResponse().getResponseHeaderValue("X-BLUEOCEAN-JWT");

        Assert.assertNotNull(token);


        JsonWebStructure jsonWebStructure = JsonWebStructure.fromCompactSerialization(token);

        Assert.assertTrue(jsonWebStructure instanceof JsonWebSignature);

        JsonWebSignature jsw = (JsonWebSignature) jsonWebStructure;


        String kid = jsw.getHeader("kid");

        Assert.assertNotNull(kid);

        page = webClient.goTo("jwt-auth/jwks/"+kid+"/", "application/json");

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
}
