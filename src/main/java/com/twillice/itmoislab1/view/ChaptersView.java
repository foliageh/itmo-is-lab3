package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.Chapter;
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
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named @ViewScoped
@Getter @Setter
public class ChaptersView implements Serializable {
    private List<Chapter> chapters;
    private List<SpaceMarine> relatedSpaceMarines = new ArrayList<>();

    private Chapter selectedChapter;
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
        chapters = chapterService.findAll();
    }

    public void openNew() {
        selectedChapter = new Chapter();
    }

    public void saveChapter() {
        if (selectedChapter.getId() == null) {
            chapterService.create(selectedChapter);
            MessageManager.info("Chapter added.", null);
        } else {
            chapterService.update(selectedChapter);
            MessageManager.info("Chapter updated.", null);
        }

        // PrimeFaces.current().executeScript("PF('manageChapterDialog').hide()");

        refreshData();
        PrimeFaces.current().ajax().update("@widgetVar(dtChapters)");
    }

    public void deleteSelectedChapter() {
        chapterService.remove(selectedChapter);
        selectedChapter = null;
        MessageManager.info("Chapter removed.", null);

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
        spaceMarineService.update(event.getObject());
        PrimeFaces.current().ajax().update(":dialogs:delete-chapter-button");
        PrimeFaces.current().ajax().update(":dialogs:related-space-marines");
    }
}
