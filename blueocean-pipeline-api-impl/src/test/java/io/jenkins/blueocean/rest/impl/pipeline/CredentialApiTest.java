package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.ViewCredentialsAction;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.ExtensionList;
import hudson.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class CredentialApiTest extends PipelineBaseTest {

    @Test
    public void listCredentials() throws IOException {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));
        systemStore.addCredentials(systemStore.getDomainByName("domain1"), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null,null, "admin", "pass$wd"));


        CredentialsStoreAction credentialsStoreAction = ExtensionList.lookup(ViewCredentialsAction.class).get(0).getStore("system");
        CredentialsStoreAction.DomainWrapper domainWrapper = credentialsStoreAction.getDomain("domain1");
        CredentialsStoreAction.CredentialsWrapper credentialsWrapper = domainWrapper.getCredentialsList().get(0);


        List<Map>  creds = get("/organizations/jenkins/credentials/system/domains/domain1/credentials/", List.class);
        Assert.assertEquals(1, creds.size());
        Map cred = creds.get(0);
        Assert.assertNotNull(cred.get("id"));

        Map cred1 = get("/organizations/jenkins/credentials/system/domains/domain1/credentials/"+cred.get("id")+"/");

        Assert.assertEquals(credentialsWrapper.getId(),cred1.get("id"));
        Assert.assertEquals(credentialsWrapper.getTypeName(),cred1.get("typeName"));
        Assert.assertEquals(credentialsWrapper.getDisplayName(),cred1.get("displayName"));
        Assert.assertEquals(credentialsWrapper.getFullName(),cred1.get("fullName"));
        Assert.assertEquals(String.format("%s:%s:%s", credentialsWrapper.getDisplayName(), credentialsWrapper.getDomain().getUrlName(), credentialsWrapper.getTypeName()),cred1.get("description"));
        Assert.assertEquals(credentialsWrapper.getDomain().getUrlName(),cred1.get("domain"));
    }

    @Test
    public void listAllCredentials() throws IOException {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));
        systemStore.addDomain(new Domain("domain2", null, null));
        systemStore.addCredentials(systemStore.getDomainByName("domain1"), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null,null, "admin", "pass$wd"));
        systemStore.addCredentials(systemStore.getDomainByName("domain2"), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null,null, "joe", "pass$wd"));

        CredentialsStoreAction credentialsStoreAction = ExtensionList.lookup(ViewCredentialsAction.class).get(0).getStore("system");
        CredentialsStoreAction.DomainWrapper domain1 = credentialsStoreAction.getDomain("domain1");
        CredentialsStoreAction.DomainWrapper domain2 = credentialsStoreAction.getDomain("domain2");

        CredentialsStoreAction.CredentialsWrapper credentials1 = domain1.getCredentialsList().get(0);
        CredentialsStoreAction.CredentialsWrapper credentials2 = domain2.getCredentialsList().get(0);
        List<Map>  creds = get("/search?q=type:credential;organization:jenkins", List.class);
        Assert.assertEquals(2, creds.size());
        Assert.assertEquals(credentials1.getId(), creds.get(0).get("id"));
        Assert.assertEquals(credentials2.getId(), creds.get(1).get("id"));

        creds = get("/search?q=type:credential;organization:jenkins;domain:domain2", List.class);
        Assert.assertEquals(1, creds.size());
        Assert.assertEquals(credentials2.getId(), creds.get(0).get("id"));
    }

    @Test
    public void createSshCredentialUsingDirectSsh() throws IOException {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String, Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("privateKeySource", ImmutableMap.of(
                                        "privateKey", "abcabc1212",
                                        "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                                .put("passphrase", "ssh2")
                                .put("scope", "GLOBAL")
                                .put("description", "ssh2 desc")
                                .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                                .put("username", "ssh2").build()
                )
                , 201);
        Assert.assertEquals("SSH Username with private key", resp.get("typeName"));
        Assert.assertEquals("domain1", resp.get("domain"));
    }

    @Test
    public void createUsingUsernamePassword() throws IOException {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String, Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("password", "abcd")
                                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("scope", "GLOBAL")
                                .put("description", "joe desc")
                                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("username", "joe").build()
                )
                , 201);
        Assert.assertEquals("Username with password", resp.get("typeName"));
        Assert.assertEquals("domain1", resp.get("domain"));
    }

    @Test
    public void createUsingUsernamePasswordInUserStore() throws IOException, UnirestException {
        User user = login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/credentials/user/")
                .data(                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("password", "abcd")
                                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("scope", "USER")
                                .put("description", "joe desc")
                                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("username", "joe").build()
                        )
                )
                .build(Map.class);

        Assert.assertEquals("Username with password", resp.get("typeName"));
        Assert.assertEquals("blueocean-domain", resp.get("domain"));
    }

    @Test
    public void createSshCredentialUsingDirectSshInUserStore() throws IOException, UnirestException {
        User user = login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/credentials/user/")
                .data(                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("privateKeySource", ImmutableMap.of(
                                        "privateKey", "abcabc1212",
                                        "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                                .put("passphrase", "ssh2")
                                .put("scope", "USER")
                                .put("domain","blueocean-git-domain")
                                .put("description", "ssh2 desc")
                                .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                                .put("username", "ssh2").build()
                        )
                )
                .build(Map.class);

        Assert.assertEquals("SSH Username with private key", resp.get("typeName"));
        Assert.assertEquals("blueocean-git-domain", resp.get("domain"));

        String credId = (String) resp.get("id");

        assertNotNull(credId);

        String credUri = getUrlFromHref(getHrefFromLinks(resp, "self"));
        resp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .get(credUri)
                .build(Map.class);

        Assert.assertEquals("SSH Username with private key", resp.get("typeName"));
        Assert.assertEquals("blueocean-git-domain", resp.get("domain"));
    }
}
