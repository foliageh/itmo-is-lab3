package com.twillice.itmoislab1.security;

import com.twillice.itmoislab1.model.User;
import jakarta.security.enterprise.CallerPrincipal;
import lombok.Getter;

@Getter
public class UserPrincipal extends CallerPrincipal {
    private final User user;

    public UserPrincipal(User user) {
        super(user.getUsername());
        this.user = user;
    }
}