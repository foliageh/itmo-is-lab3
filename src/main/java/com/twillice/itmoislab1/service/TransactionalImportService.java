package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.SpaceMarine;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// since rollback on @Transactional method apparently doesn't work with Jakarta EE + Wildfly + PostgreSQL, here we are...
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TransactionalImportService {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private UserTransaction ut;

    @Inject
    private Security security;

    public Chapter findChapterByName(String name) {
        List<Chapter> chapters = em.createQuery("from Chapter c where c.name = :name", Chapter.class)
                .setParameter("name", name)
                .getResultList();
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    public int validateAndImportChapters(List<Chapter> chapters) throws Exception {
        try {
            boolean importChaptersOnly = ut.getStatus() == Status.STATUS_NO_TRANSACTION;

            if (importChaptersOnly) {
                ut.setTransactionTimeout(3);
                ut.begin();
            }

            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            for (Chapter chapter : chapters) {
                var violations = validator.validate(chapter);
                if (!violations.isEmpty()) {
                    StringBuilder errors = new StringBuilder("Validation errors for chapter with name \"" + chapter.getName() + "\": ");
                    for (var violation : violations)
                        errors.append(violation.getMessage()).append("; "); //.append(violation.getPropertyPath())
                    MessageManager.error("Validation of some chapters failed", errors.toString());
                    return 0;
                }
            }

            Map<String, Chapter> uniqueChapters = new HashMap<>();  // Chapter.name : Chapter
            for (Chapter chapter : chapters) {
                Chapter chapterWithSameName = uniqueChapters.put(chapter.getName(), chapter);
                if (chapterWithSameName != null && !chapterWithSameName.equalsByFields(chapter) || findChapterByName(chapter.getName()) != null) {
                    MessageManager.error("Some added chapters are not unique",
                            "Chapter with name \"" + chapter.getName() + "\" is not unique");
                    return 0;
                }
            }

            for (var chapter : chapters) {
                chapter.setCreatedBy(security.getUser());
                chapter.setCreatedTime(ZonedDateTime.now());
                em.persist(chapter);
            }

            // Imitate crash in business logic
//            if (true)
//                throw new RuntimeException();

            if (importChaptersOnly)
                ut.commit();
            return chapters.size();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
    }

    public int validateAndImportSpaceMarines(List<SpaceMarine> spaceMarines) throws Exception {
        try {
            ut.setTransactionTimeout(3);
            ut.begin();

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
            int chaptersImportedCount = validateAndImportChapters(chapters);
            if (chaptersImportedCount == 0)
                return 0;

            for (var spaceMarine : spaceMarines) {
                spaceMarine.setCreatedBy(security.getUser());
                spaceMarine.setCreatedTime(ZonedDateTime.now());
                em.persist(spaceMarine);
            }

            ut.commit();
            return spaceMarines.size();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
    }
}
