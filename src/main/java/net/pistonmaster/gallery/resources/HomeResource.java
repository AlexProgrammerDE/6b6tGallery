package net.pistonmaster.gallery.resources;

import io.dropwizard.jackson.Jackson;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.gallery.User;
import net.pistonmaster.gallery.api.PostResponse;
import net.pistonmaster.gallery.storage.PostStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@jakarta.ws.rs.Path("/home")
public class HomeResource {
    private final String publicFilesPath;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PostResponse> getHomePosts() {
        List<PostResponse> storageResponse = new ArrayList<>();

        try {
            try (Stream<Path> paths = Files.list(Paths.get(publicFilesPath))) {
                List<Path> folders = paths.collect(Collectors.toList());

                Collections.shuffle(folders);
                Random random = new Random();

                // Add 20 random posts from the folders
                for (int i = 0; i < 20; i++) {
                    int randomIndex = random.nextInt(folders.size());
                    Path folder = folders.get(randomIndex);

                    if (storageResponse.stream().map(PostResponse::imageId).anyMatch(folder.getFileName().toString()::equals)) {
                        continue;
                    }

                    PostStorage postStorage = Jackson.newObjectMapper().readValue(Files.readString(folder.resolve("data.json")), PostStorage.class);
                    storageResponse.add(new PostResponse(
                            postStorage.getImage().getId(),
                            postStorage.getImage().getExtension(),
                            postStorage.getImage().getWidth(),
                            postStorage.getImage().getHeight(),
                            postStorage.getDescription(),
                            new User(postStorage.getAuthor()).generateUserDataResponse()
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return storageResponse;
    }
}
