package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.collect.ImmutableMap;
import hudson.ExtensionList;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class CredentialApiTest extends PipelineBaseTest {


//    @Test
    public void createCredential(){
        post("/organizations/jenkins/credentials/system/domains/_/credentials/", ImmutableMap.of("password", "y",
                "$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl",
                "username","xx",
                "scope", "GLOBAL"));

//        get("/organizations/jenkins/credentials/system/domains/_/credentials/");
    }

    @Test
    public void listCredentials() throws IOException {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));
        systemStore.addCredentials(systemStore.getDomainByName("domain1"), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null,null, "admin", "pass$wd"));
        List<Map>  creds = get("/organizations/jenkins/credentials/system/domains/domain1/credentials/", List.class);
        Assert.assertEquals(1, creds.size());
        Map cred = creds.get(0);
        Assert.assertNotNull(cred.get("id"));

        Map cred1 = get("/organizations/jenkins/credentials/system/domains/domain1/credentials/"+cred.get("id")+"/");

        Assert.assertEquals(cred.get("id"),cred1.get("id"));
        Assert.assertEquals(cred.get("typeName"),cred1.get("typeName"));
        Assert.assertEquals(cred.get("displayName"),cred1.get("displayName"));
        Assert.assertEquals(cred.get("fullName"),cred1.get("fullName"));


//        get("/organizations/jenkins/credentials/system/domains/_/credentials/");
    }
}
