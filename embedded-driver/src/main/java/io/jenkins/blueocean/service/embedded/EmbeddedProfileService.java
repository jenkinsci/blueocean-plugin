package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import io.jenkins.blueocean.api.profile.AuthenticateRequest;
import io.jenkins.blueocean.api.profile.AuthenticateResponse;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.api.profile.CreateOrganizationResponse;
import io.jenkins.blueocean.api.profile.FindUsersRequest;
import io.jenkins.blueocean.api.profile.FindUsersResponse;
import io.jenkins.blueocean.api.profile.GetOrganizationRequest;
import io.jenkins.blueocean.api.profile.GetOrganizationResponse;
import io.jenkins.blueocean.api.profile.GetUserDetailsRequest;
import io.jenkins.blueocean.api.profile.GetUserDetailsResponse;
import io.jenkins.blueocean.api.profile.GetUserRequest;
import io.jenkins.blueocean.api.profile.GetUserResponse;
import io.jenkins.blueocean.api.profile.ProfileService;
import io.jenkins.blueocean.api.profile.model.Organization;
import io.jenkins.blueocean.api.profile.model.User;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.security.AuthenticationProvider;
import io.jenkins.blueocean.security.Identity;
import io.jenkins.blueocean.security.LoginDetails;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.security.LoginDetailsProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ProfileService} implementation to be used embedded as plugin
 *
 * @author Vivek Pandey
 */
@Extension
public class EmbeddedProfileService extends AbstractEmbeddedService implements ProfileService{

    @Nonnull
    @Override
    public GetUserResponse getUser(@Nonnull Identity identity, @Nonnull GetUserRequest request) {
        hudson.model.User user = hudson.model.User.get(request.id, false, Collections.EMPTY_MAP);

        if(user == null){
            return new GetUserResponse(new User(Identity.ANONYMOUS.getName(), Identity.ANONYMOUS.getName()));
        }

        if(!user.getId().equals(request.id)){
            throw new ServiceException.NotFoundException(String.format("User %s not found", request.id));
        }

        return new GetUserResponse(new User(user.getId(), user.getFullName()));
    }

    @Nonnull
    @Override
    public GetUserDetailsResponse getUserDetails(@Nonnull Identity identity, @Nonnull GetUserDetailsRequest request) {
        hudson.model.User user = hudson.model.User.get(request.id, false, Collections.EMPTY_MAP);
        if (user == null) {
            throw new ServiceException.NotFoundException(String.format("Request user %s not found", request.id));
        }

        if(!user.getId().equals(request.id)){
            throw new ServiceException.NotFoundException(String.format("User %s not found", request.id));
        }

        //TODO: How to get user's email in Jenkins
        return new GetUserDetailsResponse(new UserDetails(user.getId(), user.getFullName(),"none",
                Collections.<LoginDetails>emptySet()));
    }

    @Nonnull
    @Override
    public GetOrganizationResponse getOrganization(@Nonnull Identity identity, @Nonnull GetOrganizationRequest request) {
        validateOrganization(request.name);
        return new GetOrganizationResponse(new Organization(jenkins.getDisplayName()));
    }

    @Nonnull
    @Override
    public CreateOrganizationResponse createOrganization(@Nonnull Identity identity, @Nonnull CreateOrganizationRequest request) {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @Nonnull
    @Override
    public FindUsersResponse findUsers(@Nonnull Identity identity, @Nonnull FindUsersRequest request) {
        validateOrganization(request.organization);
        //TODO: hudson.model.User.getAll() could be expensive, need to find better way to do it
        List<User> users = new ArrayList<User>();
        for(hudson.model.User u:hudson.model.User.getAll()){
            users.add(new User(u.getId(), u.getDisplayName()));
        }
        return new FindUsersResponse(users, null, null);
    }

    @Nonnull
    @Override
    public AuthenticateResponse authenticate(@Nonnull AuthenticateRequest request) {
        LoginDetailsProvider loginDetailsProvider = AuthenticationProvider.getLoginDetailsProvider(request.loginDetails.getClass());
        return new AuthenticateResponse(loginDetailsProvider.authenticate(request.loginDetails));
    }
}
