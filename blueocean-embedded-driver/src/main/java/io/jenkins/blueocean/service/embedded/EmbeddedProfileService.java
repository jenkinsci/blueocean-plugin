package io.jenkins.blueocean.service.embedded;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.api.profile.CreateOrganizationResponse;
import io.jenkins.blueocean.api.profile.CreateUserRequest;
import io.jenkins.blueocean.api.profile.CreateUserResponse;
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
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.security.Credentials;
import io.jenkins.blueocean.security.Identity;
import io.jenkins.blueocean.service.embedded.properties.CredentialsProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ProfileService} implementation to be used embedded as plugin
 *
 * @author Vivek Pandey
 */
public class EmbeddedProfileService extends AbstractEmbeddedService implements ProfileService {

    private final LoadingCache<String, Lock> userCreationLocks = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<String, Lock>() {
        @Override
        public Lock load(String userId) throws Exception {
            return new ReentrantLock();
        }
    });

    @Nonnull
    @Override
    public GetUserResponse getUser(@Nonnull Identity identity, @Nonnull GetUserRequest request) {
        hudson.model.User user = getJenkinsUser(request.id);
        return new GetUserResponse(Mapper.mapUser(user));
    }

    @Nonnull
    @Override
    public GetUserDetailsResponse getUserDetails(@Nonnull Identity identity, @Nonnull GetUserDetailsRequest request) {
        hudson.model.User user;
        if (request.byUserId != null) {
            user = getJenkinsUser(request.byUserId);
        } else if (request.byCredentials != null) {
            user = getJenkinsUserByCredentials(request.byCredentials);
        } else {
            throw new ServiceException.UnprocessableEntityException("did not specify userId or credentials");
        }
        return new GetUserDetailsResponse(Mapper.mapUserDetails(user));
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
        List<User> users = new ArrayList<User>();
        for(hudson.model.User u:hudson.model.User.getAll()){
            users.add(new User(u.getId(), u.getDisplayName()));
        }
        return new FindUsersResponse(users, null, null);
    }

    @Override
    @Nonnull
    public CreateUserResponse createUser(@Nonnull Identity identity, @Nonnull CreateUserRequest request) {
        hudson.model.User user;
        String userId = Objects.firstNonNull(request.email, request.fullName);
        if (Strings.isNullOrEmpty(userId)) {
            throw new ServiceException.UnprocessableEntityException("could not synthesise a user id");
        }
        // Try to create a user with the requested user id
        // Lock is needed to force Jenkins to not synthesise the ID if there is a conflict
        Lock lock = userCreationLocks.getUnchecked(userId);
        if (lock.tryLock()) {
            try {
                hudson.model.User existingUser;
                try {
                    existingUser = getJenkinsUser(userId);
                } catch (NotFoundException e) {
                    existingUser = null;
                }
                if (existingUser == null) {
                    user = hudson.model.User.get(userId, true, ImmutableMap.of());
                    if (user == null) {
                        throw new ServiceException.UnexpectedErrorExpcetion("created user was null");
                    }
                } else {
                    throw new ServiceException.UnprocessableEntityException("user id already exists");
                }
            } finally {
                lock.unlock();
            }
        } else {
            throw new ServiceException.TooManyRequestsException("could not create user");
        }
        return new CreateUserResponse(Mapper.mapUserDetails(Mapper.mapJenkinsUser(user, request.email, request.fullName, request.credentials)));
    }

    /** Safe way to query a user without creating it at the same time */
    hudson.model.User getJenkinsUser(String email) {
        hudson.model.User user = hudson.model.User.get(email, false, ImmutableMap.of());
        if (user == null) {
            throw new ServiceException.NotFoundException("could not find user");
        }
        return user;
    }

    hudson.model.User getJenkinsUserByCredentials(Credentials credentials) {
        for (hudson.model.User user : hudson.model.User.getAll()) {
            Set<Credentials> credentialsSet = Mapper.mapCredentials(user);
            if (Iterables.find(credentialsSet, credentials.identityPredicate(), null) != null) {
                return user;
            }
        }
        throw new ServiceException.NotFoundException("could not find user");
    }
}
