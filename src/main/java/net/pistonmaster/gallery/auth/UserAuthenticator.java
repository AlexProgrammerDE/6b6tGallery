package net.pistonmaster.gallery.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jackson.Jackson;
import keywhiz.hkdf.Hkdf;
import net.pistonmaster.gallery.User;
import net.pistonmaster.gallery.utils.JWTToken;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Optional;

public class UserAuthenticator implements Authenticator<String, User> {
    private final byte[] key;

    public UserAuthenticator(String jwtTokenSecret) {
        Hkdf hkdf = Hkdf.usingDefaults();
        SecretKey initialKey = hkdf.extract(null, jwtTokenSecret.getBytes(StandardCharsets.UTF_8));

        key = hkdf.expand(initialKey, "NextAuth.js Generated Encryption Key".getBytes(StandardCharsets.UTF_8), 32);
    }

    @Override
    public Optional<User> authenticate(String token) {
        try {
            JWEObject jweObject = JWEObject.parse(token);

            jweObject.decrypt(new DirectDecrypter(key));

            JWTToken jwt = Jackson.newObjectMapper().readValue(jweObject.getPayload().toString(), JWTToken.class);

            return Optional.of(new User(jwt));
        } catch (JOSEException | ParseException | JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
