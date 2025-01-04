package com.twillice.itmoislab1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.FacesConfig;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

@FacesConfig
@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login",
                useForwardToLogin = false, // redirect to login page when access a protected view is more clear for user than forward
                errorPage = "" // don't want separate error page, instead redisplay the original page with the error messages rendered on it
        )
)
public class ApplicationConfig {
}
