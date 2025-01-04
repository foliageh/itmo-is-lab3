package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.ChaptersImportHistory;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.ZonedDateTime;
import java.util.*;

@Stateless
public class ChapterService {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Security security;

    public Chapter find(Long id) {
        return em.find(Chapter.class, id);
    }

    public List<Chapter> findAll() {
        return em.createQuery("from Chapter", Chapter.class).getResultList();
    }

    public boolean existsByName(String name) {
        return em.createQuery("select count(*) from Chapter c where c.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;
    }

    public Chapter findByName(String name) {
        List<Chapter> chapters = em.createQuery("from Chapter c where c.name = :name", Chapter.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    @Transactional(rollbackOn = Exception.class)
    public Long create(Chapter chapter) {
        if (existsByName(chapter.getName())) {
            MessageManager.error("Chapter with this name already exists.", null);
            return null;
        }

        chapter.setCreatedBy(security.getUser());
        chapter.setCreatedTime(ZonedDateTime.now());
        em.persist(chapter);

        MessageManager.info("Chapter created.", null);
        return chapter.getId();
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean update(Chapter chapterInitialState, Chapter chapter) {
        Chapter chapterSavedVersion = find(chapter.getId());
        if (chapterSavedVersion == null) {
            MessageManager.error("Editable object has been deleted!", "Just close this window.");
            return false;
        }

        if (!chapterSavedVersion.equalsByFields(chapterInitialState)) {
            MessageManager.info("Editable object has been modified by another user",
                    "Open the object in a new window and edit it there.");
            return false;
        }

        if (!chapterSavedVersion.getName().equals(chapter.getName()) && existsByName(chapter.getName())) {
            MessageManager.error("Chapter with this name already exists.", null);
            return false;
        }

        chapter.setUpdateFields(security.getUser(), ZonedDateTime.now());
        em.merge(chapter);

        MessageManager.info("Chapter updated.", null);
        return true;
    }

    @Transactional(rollbackOn = Exception.class)
    public void remove(Chapter chapter) {
        if (find(chapter.getId()) != null)
            em.remove(em.contains(chapter) ? chapter : em.merge(chapter));
        MessageManager.info("Chapter removed.", null);
    }

    @Transactional(rollbackOn = Exception.class)
    public int importAll(List<Chapter> chapters) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        for (Chapter chapter : chapters) {
            var violations = validator.validate(chapter);
            if (!violations.isEmpty()) {
                StringBuilder errors = new StringBuilder("Validation errors for chapter with name \"" + chapter.getName() + "\": ");
                for (var violation : violations)
                    errors.append(violation.getMessage()).append("; "); //.append(violation.getPropertyPath())

                MessageManager.error("Validation of some chapters failed", errors.toString());
                addImportHistory(0);
                return 0;
            }
        }

        Map<String, Chapter> uniqueChapters = new HashMap<>();  // Chapter.name : Chapter
        for (Chapter chapter : chapters) {
            Chapter chapterWithSameName = uniqueChapters.put(chapter.getName(), chapter);
            if (chapterWithSameName != null && !chapterWithSameName.equalsByFields(chapter) || existsByName(chapter.getName())) {
                MessageManager.error("Some added chapters are not unique",
                        "Chapter with name \"" + chapter.getName() + "\" is not unique");
                addImportHistory(0);
                return 0;
            }
        }

        for (var chapter : chapters) {
            chapter.setCreatedBy(security.getUser());
            chapter.setCreatedTime(ZonedDateTime.now());
            em.persist(chapter);
        }
        addImportHistory(chapters.size());

        MessageManager.info("Chapters import succeeded", chapters.size() + " chapters added.");
        return chapters.size();
    }

    public List<ChaptersImportHistory> getImportHistory() {
        return em.createQuery("from ChaptersImportHistory", ChaptersImportHistory.class).getResultList();
    }

    private Long addImportHistory(int importedCount) {
        var history = new ChaptersImportHistory();
        if (importedCount > 0) {
            history.setSuccess(true);
            history.setEntitiesAdded(importedCount);
        }

        history.setImportedBy(security.getUser());
        history.setImportedTime(ZonedDateTime.now());

        em.persist(history);
        return history.getId();
    }
}
