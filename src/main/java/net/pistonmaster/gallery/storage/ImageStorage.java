package net.pistonmaster.gallery.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageStorage {
    private String id;
    private String extension;
    private int width;
    private int height;
}
