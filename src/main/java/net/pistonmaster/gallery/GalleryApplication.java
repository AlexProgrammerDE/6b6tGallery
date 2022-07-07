package net.pistonmaster.gallery;

import com.twelvemonkeys.servlet.image.IIOProviderContextListener;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.forms.MultiPartBundle;
import lombok.Getter;
import net.pistonmaster.gallery.auth.AuthCredentialFilter;
import net.pistonmaster.gallery.auth.UserAuthenticator;
import net.pistonmaster.gallery.auth.UserAuthorizer;
import net.pistonmaster.gallery.manager.StaticFileManager;
import net.pistonmaster.gallery.resources.HomeResource;
import net.pistonmaster.gallery.resources.PostResource;
import net.pistonmaster.gallery.servlets.FileAssetServlet;
import org.eclipse.jetty.servlets.DoSFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Getter
public class GalleryApplication extends Application<GalleryConfiguration> {
    public static void main(String[] args) throws Exception {
        new GalleryApplication().run("server", "/config.yml");
    }

    @Override
    public String getName() {
        return "PistonPost";
    }

    @Override
    public void initialize(Bootstrap<GalleryConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());

        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    @Override
    public void run(GalleryConfiguration configuration,
                    Environment environment) {
        environment.jersey().register(new AuthDynamicFeature(
                new AuthCredentialFilter.Builder<User>()
                        .setAuthenticator(new UserAuthenticator(configuration.getJwtTokenSecret()))
                        .setAuthorizer(new UserAuthorizer())
                        .setPrefix("Bearer")
                        .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        // If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        ImageIO.scanForPlugins();
        environment.servlets().addServletListeners(new IIOProviderContextListener());
        environment.servlets().addFilter("DoSFilter", new DoSFilter());

        environment.jersey().register(new HomeResource(configuration.getPublicFilesPath()));

        environment.servlets().addServlet("file-assets", new FileAssetServlet("public/", "/static/", null, StandardCharsets.UTF_8)).addMapping("/static/*");
        StaticFileManager staticFileManager = new StaticFileManager(configuration.getSubmitFilesPath(), configuration.getPublicFilesPath());
        staticFileManager.init();
        environment.jersey().register(new PostResource(staticFileManager, Path.of(configuration.getSubmitFilesPath())));
    }
}
