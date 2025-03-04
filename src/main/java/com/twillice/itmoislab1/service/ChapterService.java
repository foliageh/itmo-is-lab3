package com.twillice.itmoislab1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.ChaptersImportHistory;
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
public class ChapterService extends ImportService<Chapter> {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Security security;

    @Inject
    private TransactionalImportService transactionalImportService;

    public Chapter find(Long id) {
        return em.find(Chapter.class, id);
    }

    public List<Chapter> findAll() {
        return em.createQuery("from Chapter", Chapter.class).getResultList();
    }

    public Chapter findByName(String name) {
        List<Chapter> chapters = em.createQuery("from Chapter c where c.name = :name", Chapter.class)
                .setParameter("name", name)
                .getResultList();
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    @Transactional(rollbackOn = Exception.class)
    public Long create(Chapter chapter) {
        if (findByName(chapter.getName()) != null) {
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

        if (!chapterSavedVersion.getName().equals(chapter.getName()) && findByName(chapter.getName()) != null) {
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
        if (find(chapter.getId()) != null) {
            em.remove(em.contains(chapter) ? chapter : em.merge(chapter));
            MessageManager.info("Chapter removed.", null);
        } else {
            MessageManager.info("Chapter was removed by another user.", null);
        }
    }

    @Override
    public int processImport(InputStream fileInputStream) {
        return processImport(fileInputStream, new TypeReference<>() {});
    }

    @Override
    public int validateAndImport(List<Chapter> chapters) throws Exception {
        return transactionalImportService.validateAndImportChapters(chapters);
    }

    @Override
    protected Long addImportHistory(int importedCount, String fileName) {
        return addImportHistory(new ChaptersImportHistory(), importedCount, fileName);
    }

    @Override
    public List<ChaptersImportHistory> getImportHistory() {
        return em.createQuery("from ChaptersImportHistory", ChaptersImportHistory.class).getResultList();
    }
}
