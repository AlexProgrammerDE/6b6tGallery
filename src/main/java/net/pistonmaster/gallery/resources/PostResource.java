package net.pistonmaster.gallery.resources;

import io.dropwizard.auth.Auth;
import io.dropwizard.jackson.Jackson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.gallery.User;
import net.pistonmaster.gallery.api.SuccessResponse;
import net.pistonmaster.gallery.manager.StaticFileManager;
import net.pistonmaster.gallery.storage.ImageStorage;
import net.pistonmaster.gallery.storage.PostStorage;
import net.pistonmaster.gallery.storage.UserDataStorage;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@jakarta.ws.rs.Path("/post")
public class PostResource {
    private final StaticFileManager staticFileManager;
    private final java.nio.file.Path staticFilesPath;

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public SuccessResponse createPost(@Auth User user, @FormDataParam("description") String description, FormDataMultiPart multiPart) {
        validateDescription(description);
        description = description.trim();

        FormDataBodyPart imagePart = multiPart.getField("image");
        if (imagePart == null) {
            throw new WebApplicationException("Your request is missing data!", 400);
        }

        ImageStorage imageStorage = staticFileManager.uploadImage(imagePart.getValueAs(byte[].class), imagePart.getContentDisposition());

        PostStorage postStorage = new PostStorage(imageStorage, new UserDataStorage(user.getId(), user.getName(), user.getEmail(), user.getRoles()), description);

        Path dataPath = staticFilesPath.resolve(imageStorage.getId()).resolve("data.json");

        try (OutputStream out = Files.newOutputStream(dataPath)) {
            Jackson.newObjectMapper().writeValue(out, postStorage);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }

        return new SuccessResponse(true);
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new WebApplicationException("Your request is missing data!", 400);
        }

        if (description.length() > 100) {
            throw new WebApplicationException("Your description is too long!", 400);
        }
    }
}
