package io.jenkins.blueocean.security;

/**
 * This action displays the authentication providers UI
 * The AuthenticationAction authenticates the post from this Action or GET from an OAuth flow
 */
public class LoginAction {

//    public ExtensionList<AuthenticationProvider> getProviders() {
//        Jenkins j = Jenkins.getInstance();
//        if(j == null) {
//            throw new IllegalStateException("jenkins instance null");
//        }
//        return j.getExtensionList(AuthenticationProvider.class);
//    }

    public static String getPath() { return "/loginAction"; }
}

