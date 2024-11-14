package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.*;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.service.ChapterService;
import com.twillice.itmoislab1.service.SpaceMarineService;
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
import java.util.stream.Collectors;

@Named @ViewScoped
@Getter @Setter
public class SpaceMarinesView implements Serializable {
    private List<SpaceMarine> spaceMarines;

    private SpaceMarine selectedSpaceMarine;
    private List<SpaceMarine> filteredSpaceMarines;
    private List<FilterMeta> filterBy;

    @Inject
    private SpaceMarineService spaceMarineService;
    @Inject
    private ChapterService chapterService;

    @Inject
    private Security security;
    private User user;

    @PostConstruct
    public void init() {
        filterBy = new ArrayList<>();
        user = security.getUser();
        refreshData();
    }

    public void refreshData() {
        spaceMarines = spaceMarineService.findAll();
    }

    public void openNew() {
        selectedSpaceMarine = new SpaceMarine();
    }

    public void saveSpaceMarine() {
        if (selectedSpaceMarine.getId() == null) {
            spaceMarineService.create(selectedSpaceMarine);
            MessageManager.info("Space marine added.", null);
        } else {
            spaceMarineService.update(selectedSpaceMarine);
            MessageManager.info("Space marine updated.", null);
        }

        // PrimeFaces.current().executeScript("PF('manageSpaceMarineDialog').hide()");

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarines)");
    }

    public void deleteSelectedSpaceMarine() {
        spaceMarineService.remove(selectedSpaceMarine);
        selectedSpaceMarine = null;
        MessageManager.info("Space marine removed.", null);

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarines)");
    }

    public List<Chapter> suggestChapters(String query) {
        String q = query.toLowerCase();
        return chapterService.findAll().stream()
                .filter(chapter -> chapter.getName().toLowerCase().contains(q) || chapter.getId().toString().equals(q))
                .collect(Collectors.toList());
    }

    public boolean isSelectedSpaceMarineCreatedByUser() {
        if (selectedSpaceMarine == null || selectedSpaceMarine.getId() == null)
            return true;
        return selectedSpaceMarine.isCreatedByUser(user);
    }

    public boolean isSelectedSpaceMarineEditable() {
        if (selectedSpaceMarine == null || selectedSpaceMarine.getId() == null)
            return true;
        return selectedSpaceMarine.isEditableByUser(user);
    }

    public AstartesCategory[] getCategories() {
        return AstartesCategory.values();
    }

    public Weapon[] getWeapons() {
        return Weapon.values();
    }
}
