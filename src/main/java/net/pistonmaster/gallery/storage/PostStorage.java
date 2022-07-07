package net.pistonmaster.gallery.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostStorage {
    private ImageStorage image;
    private UserDataStorage author;
    private String description;
}
