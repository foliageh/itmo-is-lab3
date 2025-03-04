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

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

@Stateless
public class SpaceMarineService extends ImportService<SpaceMarine> {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Security security;

    @Inject
    private TransactionalImportService transactionalImportService;
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

    @Override
    public int processImport(InputStream fileInputStream) {
        return processImport(fileInputStream, new TypeReference<>() {});
    }

    @Override
    public int validateAndImport(List<SpaceMarine> spaceMarines) throws Exception {
        return transactionalImportService.validateAndImportSpaceMarines(spaceMarines);
    }

    @Override
    protected Long addImportHistory(int importedCount, String fileName) {
        return addImportHistory(new SpaceMarinesImportHistory(), importedCount, fileName);
    }

    @Override
    public List<SpaceMarinesImportHistory> getImportHistory() {
        return em.createQuery("from SpaceMarinesImportHistory", SpaceMarinesImportHistory.class).getResultList();
    }
}
