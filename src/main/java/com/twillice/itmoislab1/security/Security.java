package com.twillice.itmoislab1.security;

import com.twillice.itmoislab1.model.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Named
@RequestScoped
public class Security {
    @Inject
    private SecurityContext securityContext;
    @Inject
    private ExternalContext externalContext;

    public boolean hasAccessToWebResource(String resource) {
        return securityContext.hasAccessToWebResource(resource + ".xhtml", "GET");
    }

    public User getUser() {
        return securityContext.getPrincipalsByType(UserPrincipal.class).stream().map(UserPrincipal::getUser).findAny().orElse(null);
    }

    public AuthenticationStatus authenticateUser(User user) {
        return securityContext.authenticate(
                (HttpServletRequest) externalContext.getRequest(),
                (HttpServletResponse) externalContext.getResponse(),
                AuthenticationParameters.withParams().credential(
                        new UsernamePasswordCredential(
                                user.getUsername(),
                                PasswordEncoder.encode(user.getPassword())))
        );
    }

    public String getMainPageUrlForUser(User user) {
        switch (user.getRole()) {
            case NEW: return "/pending_registration";
            case ADMIN: return "/admin/users";
            default: return "/main/space_marine";
        }
    }

    public String getMainPageUrlForAuthenticatedUser() {
        return getMainPageUrlForUser(getUser());
    }

    public void redirectAuthenticatedUserToMainPage() throws IOException {
        String mainPageUrl = getMainPageUrlForAuthenticatedUser();
        externalContext.redirect(externalContext.getRequestContextPath() + mainPageUrl);
    }
}
