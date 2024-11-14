package com.twillice.itmoislab1.util;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public class MessageManager {
    public static void message(FacesMessage.Severity severity, String summary, String detail) {
        var context = FacesContext.getCurrentInstance();
        context.addMessage("growl", new FacesMessage(severity, summary, detail));
        context.getExternalContext().getFlash().setKeepMessages(true);
    }

    public static void error(String message, String detail) {
        message(FacesMessage.SEVERITY_ERROR, message, detail);
    }

    public static void info(String message, String detail) {
        message(FacesMessage.SEVERITY_INFO, message, detail);
    }
}
