package net.pistonmaster.gallery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.gallery.api.UserDataResponse;
import net.pistonmaster.gallery.storage.UserDataStorage;
import net.pistonmaster.gallery.utils.JWTToken;
import net.pistonmaster.gallery.utils.MD5Util;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class User implements Principal {
    private final String id;
    private final String name;
    private final String email;
    private final String avatar;
    private final String profile;
    private final Set<String> roles = new HashSet<>();

    public User(JWTToken userDataStorage) {
        this(
                userDataStorage.getSub(),
                userDataStorage.getName(),
                userDataStorage.getEmail(),
                generateAvatar(userDataStorage.getEmail()),
                generateProfile(userDataStorage.getEmail())
        );
    }

    public User(UserDataStorage userDataStorage) {
        this(
                userDataStorage.getId(),
                userDataStorage.getName(),
                userDataStorage.getEmail(),
                generateAvatar(userDataStorage.getEmail()),
                generateProfile(userDataStorage.getEmail())
        );
    }

    private static String generateAvatar(String email) {
        return String.format(
                "https://www.gravatar.com/avatar/%s?d=retro",
                MD5Util.md5Hex(email.toLowerCase())
        );
    }

    private static String generateProfile(String email) {
        return String.format(
                "https://www.gravatar.com/%s",
                MD5Util.md5Hex(email.toLowerCase())
        );
    }

    public UserDataResponse generateUserDataResponse() {
        return new UserDataResponse(name, avatar, profile);
    }
}
