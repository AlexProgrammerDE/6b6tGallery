package net.pistonmaster.gallery;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.constraints.NotEmpty;

public class GalleryConfiguration extends Configuration {
    @NotEmpty
    private String jwtTokenSecret;

    @NotEmpty
    private String version;

    @NotEmpty
    private String submitFilesPath;

    @NotEmpty
    private String publicFilesPath;

    @JsonProperty
    public String getJwtTokenSecret() {
        return jwtTokenSecret;
    }

    @JsonProperty
    public String getVersion() {
        return version;
    }

    @JsonProperty
    public String getSubmitFilesPath() {
        return submitFilesPath;
    }

    @JsonProperty
    public String getPublicFilesPath() {
        return publicFilesPath;
    }
}
