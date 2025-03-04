package com.twillice.itmoislab1.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.ChaptersImportHistory;
import com.twillice.itmoislab1.model.SpaceMarine;
import com.twillice.itmoislab1.model.User;
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
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.StreamedContent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named @ViewScoped
@Getter @Setter
public class ChaptersView implements Serializable {
    private List<Chapter> chapters;
    private List<SpaceMarine> relatedSpaceMarines = new ArrayList<>();

    private Chapter selectedChapter;
    private Chapter selectedChapterInitialState;
    private List<Chapter> filteredChapters;
    private List<FilterMeta> filterBy;

    @Inject
    private ChapterService chapterService;
    @Inject
    private SpaceMarineService spaceMarineService;

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
        try {
            chapters = chapterService.findAll();
        } catch (Exception e) {
            MessageManager.error("Failed to load actual data!", "Lost connection to DB");
        }
    }

    public void openNew() {
        selectedChapter = new Chapter();
    }

    public void saveChapter() {
        if (selectedChapter.getId() == null) {
            chapterService.create(selectedChapter);
        } else {
            if (chapterService.update(selectedChapterInitialState, selectedChapter))
                selectedChapterInitialState = selectedChapter.getCloneByFields();
        }

        // PrimeFaces.current().executeScript("PF('manageChapterDialog').hide()");

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtChapters)");
        PrimeFaces.current().ajax().update("@widgetVar(dtChapterChangeHistory)");
    }

    public void deleteSelectedChapter() {
        chapterService.remove(selectedChapter);
        selectedChapter = null;

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtChapters)");
    }

    public boolean isSelectedChapterCreatedByUser() {
        if (selectedChapter == null || selectedChapter.getId() == null)
            return true;
        return selectedChapter.isCreatedByUser(user);
    }

    public boolean isSelectedChapterEditable() {
        if (selectedChapter == null || selectedChapter.getId() == null)
            return true;
        return selectedChapter.isEditableByUser(user);
    }

    public List<Chapter> suggestChapterReplacements(String query) {
        String q = query.toLowerCase();
        return chapterService.findAll().stream()
                .filter(chapter -> !chapter.equals(selectedChapter)
                        && (chapter.getName().toLowerCase().contains(q) || chapter.getId().toString().equals(q)))
                .collect(Collectors.toList());
    }

    public List<SpaceMarine> getRefreshedRelatedSpaceMarinesData(Chapter chapter) {
        return spaceMarineService.findByChapter(chapter);
    }

    public boolean getCheckSelectedChapterHasUneditableRelatedSpaceMarines() {
        return relatedSpaceMarines == null || relatedSpaceMarines.stream().anyMatch(spaceMarine -> !spaceMarine.isEditableByUser(user));
    }

    public void onRelatedSpaceMarineRowEdit(RowEditEvent<SpaceMarine> event) {
        relatedSpaceMarines.remove(event.getObject());
        spaceMarineService.update(null, event.getObject());
        PrimeFaces.current().ajax().update(":dialogs:bDelete");
        PrimeFaces.current().ajax().update(":dialogs:related-space-marines");
    }

    public List<ChaptersImportHistory> getImportHistory() {
        return chapterService.getImportHistory();
    }

    public void handleDataImport(FileUploadEvent event) {
        int importedCount;

        try (InputStream inputStream = event.getFile().getInputStream()) {
            importedCount = chapterService.processImport(inputStream);
        } catch (Exception e) {
            MessageManager.error("Something went wrong with the file", e.getMessage());
            return;
        }

        if (importedCount >= 0)
            PrimeFaces.current().ajax().update("@widgetVar(dtChaptersImportHistory)");
        if (importedCount >= 1) {
            refreshData();
            PrimeFaces.current().ajax().update("@widgetVar(dtChapters)");
        }
    }

    public StreamedContent getExportFile() {
        String json;
        try {
            var objectMapper = new ObjectMapper().findAndRegisterModules();
            json = objectMapper.writeValueAsString(chapters);
        } catch (Exception e) {
            MessageManager.error("Failed to convert chapters to JSON", e.getMessage());
            return null;
        }

        try (InputStream inputStream = new ByteArrayInputStream(json.getBytes())) {
            return DefaultStreamedContent.builder()
                    .name("chapters_data.json")
                    .contentType("application/json")
                    .stream(() -> inputStream)
                    .build();
        } catch (IOException e) {
            MessageManager.error("Failed to export data", e.getMessage());
            return null;
        }
    }

    public StreamedContent downloadImportHistoryFile(ChaptersImportHistory history) {
        try (InputStream inputStream = chapterService.getImportHistoryFile(history)) {
            var baos = new ByteArrayOutputStream();
            inputStream.transferTo(baos);
            var inputStreamClone = new ByteArrayInputStream(baos.toByteArray());
            baos.close();

            return DefaultStreamedContent.builder()
                    .name("chapters_import_history" + history.getId() + ".json")
                    .contentType("application/json")
                    .stream(() -> inputStreamClone)
                    .build();
        } catch (IOException e) {
            MessageManager.error("Failed to download import history file", e.getMessage());
            return null;
        }
    }
}
