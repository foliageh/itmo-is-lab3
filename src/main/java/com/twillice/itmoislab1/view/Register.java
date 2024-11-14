package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.User;
import com.twillice.itmoislab1.security.PasswordEncoder;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.service.UserService;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;

@Named
@ViewScoped
public class Register implements Serializable {
    @Getter
    private User user;

    @Inject
    private UserService userService;

    @Inject
    private Security security;

    @PostConstruct
    public void init() throws IOException {
        if (security.getUser() != null)
            security.redirectAuthenticatedUserToMainPage();
        user = new User();
    }

    public String submit() {
        if (userService.find(user.getUsername()) != null) {
            MessageManager.error("User with this username already exists.", null);
            return null;
        }

        user.setPassword(PasswordEncoder.encode(user.getPassword()));
        userService.create(user);

        MessageManager.info("Thank you for registering!", "Wait for confirmation by the administrator. Try logging in after a while.");
        return "/login?faces-redirect=true";
    }
}
