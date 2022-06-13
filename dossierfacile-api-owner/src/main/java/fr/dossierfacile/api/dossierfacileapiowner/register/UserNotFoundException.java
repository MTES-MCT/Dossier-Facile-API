package fr.dossierfacile.api.dossierfacileapiowner.register;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Could not find user with email " + email);
    }

    public UserNotFoundException() {
        super("Could not find user");
    }

    public UserNotFoundException(Long id) {
        super("Could not find user with id " + id);
    }
}
