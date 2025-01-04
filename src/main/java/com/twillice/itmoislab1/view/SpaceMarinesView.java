package com.twillice.itmoislab1.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named @ViewScoped
@Getter @Setter
public class SpaceMarinesView implements Serializable {
    private List<SpaceMarine> spaceMarines;

    private SpaceMarine selectedSpaceMarine;
    private SpaceMarine selectedSpaceMarineInitialState;
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
        } else {
            if (spaceMarineService.update(selectedSpaceMarineInitialState, selectedSpaceMarine))
                selectedSpaceMarineInitialState = selectedSpaceMarine.getCloneByFields();
        }

        // PrimeFaces.current().executeScript("PF('manageSpaceMarineDialog').hide()");

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarines)");
        PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarineChangeHistory)");
    }

    public void deleteSelectedSpaceMarine() {
        spaceMarineService.remove(selectedSpaceMarine);
        selectedSpaceMarine = null;

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

    public List<SpaceMarinesImportHistory> getImportHistory() {
        return spaceMarineService.getImportHistory();
    }

    public void handleDataImport(FileUploadEvent event) {
        List<SpaceMarine> newSpaceMarines;
        try (InputStream inputStream = event.getFile().getInputStream()) {
            try {
                var objectMapper = new ObjectMapper().findAndRegisterModules();
                newSpaceMarines = objectMapper.readValue(inputStream, new TypeReference<>() {});
            } catch (Exception e) {
                MessageManager.error("Failed to parse JSON", null); //e.getMessage());
                return;
            }
        } catch (Exception e) {
            MessageManager.error("Something went wrong with the file", e.getMessage());
            return;
        }

        int importedCount = spaceMarineService.importAll(newSpaceMarines);

        PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarinesImportHistory)");
        if (importedCount > 0) {
            refreshData();
            PrimeFaces.current().ajax().update("@widgetVar(dtSpaceMarines)");
        }
    }

    public StreamedContent getExportFile() {
        String json;
        try {
            var objectMapper = new ObjectMapper().findAndRegisterModules();
            json = objectMapper.writeValueAsString(spaceMarines);
        } catch (Exception e) {
            MessageManager.error("Failed to convert space marines to JSON", e.getMessage());
            return null;
        }

        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        return DefaultStreamedContent.builder()
                .name("space_marines_data.json")
                .contentType("application/json")
                .stream(() -> inputStream)
                .build();
    }
}
