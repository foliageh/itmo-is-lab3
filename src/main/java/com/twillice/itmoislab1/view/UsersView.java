package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.User;
import com.twillice.itmoislab1.model.UserRole;
import com.twillice.itmoislab1.service.UserService;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named @ViewScoped
@Getter @Setter
public class UsersView implements Serializable {
    private List<User> users;

    private List<User> filteredUsers;
    private List<FilterMeta> filterBy;

    @Inject
    private UserService userService;

    @PostConstruct
    public void init() {
        filterBy = new ArrayList<>();
        refreshData();
    }

    public void refreshData() {
        users = userService.list();
    }

    public void confirmUserRegistration(User user) {
        user.setRole(UserRole.USER);
        userService.update(user);
        MessageManager.info("User #" + user.getId() + " registration confirmed.", null);
        PrimeFaces.current().ajax().update("form:messages", "form:dt-space-marines");
        PrimeFaces.current().executeScript("PF('dtUsers').clearFilters()");
    }

    public UserRole[] getRoles() {
        return UserRole.values();
    }
}
