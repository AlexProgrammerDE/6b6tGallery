package net.pistonmaster.pistonpost;

import com.mongodb.client.MongoClient;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import lombok.Getter;
import net.pistonmaster.pistonpost.auth.AdminAuthorizer;
import net.pistonmaster.pistonpost.auth.UserAuthenticator;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class PistonPostApplication extends Application<PistonPostConfiguration> {
    @Getter
    private final MongoManager mongoManager = new MongoManager();

    public static void main(String[] args) throws Exception {
        new PistonPostApplication().run("server", "/config.yml");
    }

    @Override
    public String getName() {
        return "PistonPost";
    }

    @Override
    public void initialize(Bootstrap<PistonPostConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    @Override
    public void run(PistonPostConfiguration configuration,
                    Environment environment) {
        environment.healthChecks().register("MongoDB", mongoManager);

        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new UserAuthenticator(mongoManager, configuration.getJwtTokenSecret()))
                        .setAuthorizer(new AdminAuthorizer())
                        .setPrefix("Bearer")
                        .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        // If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        mongoManager.setConnectUri(configuration.getMongoDbUri());

        final UserResource resource = new UserResource();
        environment.jersey().register(resource);
    }

    public MongoClient createClient() {
        return mongoManager.createClient();
    }
}