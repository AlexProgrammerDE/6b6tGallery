package net.pistonmaster.gallery.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class UserDataStorage {
    private String id;
    private String name;
    private String email;
    private Set<String> roles;
}
