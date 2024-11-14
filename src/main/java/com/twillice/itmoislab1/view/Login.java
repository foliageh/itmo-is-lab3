package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.User;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;

@Named
@ViewScoped
public class Login implements Serializable {
    @Getter
    private User user;

    @Inject
    private Security security;

    @PostConstruct
    public void init() throws IOException {
        if (security.getUser() != null)
            security.redirectAuthenticatedUserToMainPage();
        user = new User();
    }

    public String submit() {
        switch (security.authenticateUser(user)) {
            case SEND_CONTINUE:  // means "authentication in progress", usually occurs after user has been redirected from some page to login
                 FacesContext.getCurrentInstance().responseComplete();  // the bean should make sure response has been generated, and furthermore refrain from interacting with it
                 break;
            case SEND_FAILURE:  // authentication failed
                MessageManager.error("Authentication failed", "Username or password in invalid.");
                break;
            case SUCCESS:  // authentication succeeded
                return security.getMainPageUrlForAuthenticatedUser() + "?faces-redirect=true";
            case NOT_DONE:
                // doesn't happen here
        }
        return null;
    }
}
