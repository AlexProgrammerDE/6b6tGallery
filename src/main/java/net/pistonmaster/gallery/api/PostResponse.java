package net.pistonmaster.gallery.api;

public record PostResponse(String imageId, String extension, int width, int height, String description,
                           UserDataResponse author) {
}
