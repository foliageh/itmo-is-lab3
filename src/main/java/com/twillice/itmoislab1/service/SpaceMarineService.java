package com.twillice.itmoislab1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.SpaceMarine;
import com.twillice.itmoislab1.model.SpaceMarinesImportHistory;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class SpaceMarineService extends ImportService<SpaceMarine> {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private Security security;
    @Inject
    private ChapterService chapterService;

    public SpaceMarine find(Long id) {
        return em.find(SpaceMarine.class, id);
    }

    public List<SpaceMarine> findAll() {
        return em.createQuery("from SpaceMarine", SpaceMarine.class).getResultList();
    }

    public List<SpaceMarine> findByChapter(Chapter chapter) {
        return em.createQuery("from SpaceMarine where chapter = :chapter", SpaceMarine.class)
                .setParameter("chapter", chapter)
                .getResultList();
    }

    @Transactional(rollbackOn = Exception.class)
    public Long create(SpaceMarine spaceMarine) {
        if (chapterService.find(spaceMarine.getChapter().getId()) == null) {
            MessageManager.error("Specified chapter was deleted!", "Change the chapter.");
            return null;
        }

        spaceMarine.setCreatedBy(security.getUser());
        spaceMarine.setCreatedTime(ZonedDateTime.now());
        em.persist(spaceMarine);

        MessageManager.info("Space marine created.", null);
        return spaceMarine.getId();
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean update(SpaceMarine spaceMarineInitialState, SpaceMarine spaceMarine) {
        SpaceMarine spaceMarineSavedVersion = find(spaceMarine.getId());
        if (spaceMarineSavedVersion == null) {
            MessageManager.error("Editable object has been deleted!", "Just close this window.");
            return false;
        }

        if (spaceMarineInitialState != null && !spaceMarineSavedVersion.equalsByFields(spaceMarineInitialState)) {
            MessageManager.info("Editable object has been modified by another user",
                    "Open the object in a new window and edit it there.");
            return false;
        }

        // impossible? TODO check it
        if (chapterService.find(spaceMarine.getChapter().getId()) == null) {
            MessageManager.error("Specified chapter was deleted!", "Change the chapter.");
            return false;
        }

        spaceMarine.setUpdateFields(security.getUser(), ZonedDateTime.now());
        em.merge(spaceMarine);

        MessageManager.info("Space marine updated.", null);
        return true;
    }

    @Transactional(rollbackOn = Exception.class)
    public void remove(SpaceMarine spaceMarine) {
        if (find(spaceMarine.getId()) != null)
            em.remove(em.contains(spaceMarine) ? spaceMarine : em.merge(spaceMarine));
        MessageManager.info("Space marine removed.", null);
    }

    @Transactional(rollbackOn = Exception.class)
    public int processImport(InputStream fileInputStream) {
        try {
            return processImport(fileInputStream, new TypeReference<>() {});
        } catch (Exception e) {
            return -1;
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public int validateAndImport(List<SpaceMarine> spaceMarines) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        for (SpaceMarine spaceMarine : spaceMarines) {
            var violations = validator.validate(spaceMarine);
            if (!violations.isEmpty()) {
                StringBuilder errors = new StringBuilder("Validation errors for space marine with name \"" + spaceMarine.getName() + "\": ");
                for (var violation : violations)
                    errors.append(violation.getMessage()).append("; "); //.append(violation.getPropertyPath())
                MessageManager.error("Validation of some space marines failed", errors.toString());
                return 0;
            }
        }

        var chapters = spaceMarines.stream().map(SpaceMarine::getChapter).distinct().collect(Collectors.toList());
        int chaptersImportedCount = chapterService.validateAndImport(chapters);
        if (chaptersImportedCount == 0)
            return 0;

        for (var spaceMarine : spaceMarines) {
            spaceMarine.setCreatedBy(security.getUser());
            spaceMarine.setCreatedTime(ZonedDateTime.now());
            em.persist(spaceMarine);
        }

        return spaceMarines.size();
    }

    protected Long addImportHistory(int importedCount, String fileName) {
        return addImportHistory(new SpaceMarinesImportHistory(), importedCount, fileName);
    }

    public List<SpaceMarinesImportHistory> getImportHistory() {
        return em.createQuery("from SpaceMarinesImportHistory", SpaceMarinesImportHistory.class).getResultList();
    }
}
