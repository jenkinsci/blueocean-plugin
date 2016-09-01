package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStore;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStoreFactory;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores authentication map and makes them available in memory.
 *
 * @author Vivek Pandey
 */
@Extension(ordinal = 0)
public class SimpleJwtAuthenticationStore extends JwtAuthenticationStoreFactory implements JwtAuthenticationStore {
    private final Map<String, Authentication> authenticationMap = new ConcurrentHashMap<>();

    public void add(String id, Authentication authentication){
        if(authenticationMap.get(id) == null){
            authenticationMap.put(id, authentication);
        }
    }

    public Authentication get(String id){
        return authenticationMap.get(id);
    }

    @Override
    public Authentication getAuthentication(Map<String,Object> claims) {
        Map context = (Map) claims.get("context");
        if(context != null && context.get("authProvider") != null){
            Map authProvider = (Map) context.get("authProvider");

            if(authProvider.get("id") != null){
                String id = (String) authProvider.get("id");
                return authenticationMap.get(id);
            }
        }
        return null;
    }

    @Override
    public void store(Authentication authentication, Map<String,Object> claims) {
        String authenticationId = String.valueOf(authentication.hashCode());
        if(authenticationMap.get(authenticationId) == null){
            authenticationMap.put(authenticationId, authentication);
        }

        JSONObject provider = new JSONObject();

        provider.put("id", authenticationId);
        claims.put("authProvider", provider);
    }

    @Override
    public JwtAuthenticationStore getJwtAuthenticationStore(Map<String,Object> claims) {
        Map context = (Map) claims.get("context");
        if(context != null && context.get("authProvider") != null){
            return this;
        }
        return null;
    }

    @Override
    public JwtAuthenticationStore getJwtAuthenticationStore(Authentication authentication) {
        return this;
    }
}
