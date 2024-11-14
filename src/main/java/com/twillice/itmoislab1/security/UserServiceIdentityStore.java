package com.twillice.itmoislab1.security;

import com.twillice.itmoislab1.model.User;
import com.twillice.itmoislab1.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import java.util.Collections;

@ApplicationScoped
public class UserServiceIdentityStore implements IdentityStore {
    @Inject
    private UserService userService;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        var login = (UsernamePasswordCredential) credential;
        String username = login.getCaller();
        String password = login.getPasswordAsString();
        User user = userService.find(username, password);
        if (user != null)
            return new CredentialValidationResult(
                    new UserPrincipal(user),
                    Collections.singleton(user.getRole().name())
            );
        else return CredentialValidationResult.INVALID_RESULT;
    }
}
