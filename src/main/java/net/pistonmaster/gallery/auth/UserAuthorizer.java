package net.pistonmaster.gallery.auth;

import io.dropwizard.auth.Authorizer;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.pistonmaster.gallery.User;

import javax.annotation.Nullable;

public class UserAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role, @Nullable ContainerRequestContext requestContext) {
        return user.getRoles().contains(role);
    }
}
