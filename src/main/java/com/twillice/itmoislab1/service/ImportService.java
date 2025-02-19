package com.twillice.itmoislab1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twillice.itmoislab1.model.BaseEntity;
import com.twillice.itmoislab1.model.EntitiesImportHistory;
import com.twillice.itmoislab1.security.Security;
import com.twillice.itmoislab1.util.MessageManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Stateless
public abstract class ImportService<T extends BaseEntity> {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Security security;

    @Inject
    protected MinioService minioService;

    @Transactional(rollbackOn = Exception.class)
    protected int processImport(InputStream fileInputStream, TypeReference<List<T>> typeReference) throws Exception {
        List<T> entities;
        try {
            var baos = new ByteArrayOutputStream();
            fileInputStream.transferTo(baos);

            fileInputStream = new ByteArrayInputStream(baos.toByteArray());
            var objectMapper = new ObjectMapper().findAndRegisterModules();
            entities = objectMapper.readValue(fileInputStream, typeReference);

            fileInputStream = new ByteArrayInputStream(baos.toByteArray());
            baos.close();
        } catch (Exception e) {
            MessageManager.error("Failed to parse JSON", null);
            return -1;
        }

        String fileName = UUID.randomUUID() + ".json";

        try {
            minioService.uploadFile("temp-imports", fileName, fileInputStream);

            minioService.copyFile("temp-imports", "imports", fileName);
            int importedCount = validateAndImport(entities);

            try {
                minioService.deleteFile("temp-imports", fileName);
            } catch (Exception e) {
                System.err.println("Failed to delete temp import file");
            }
            try {
                addImportHistory(importedCount, fileName);
            } catch (Exception e) {
                System.err.println("Failed to add import history item");
                MessageManager.info("Failed to add import history item!", null);
            }
            MessageManager.info("Objects import succeeded", importedCount + " objects added.");
            return importedCount;
        } catch (Exception e) {
            try {
                minioService.deleteFile("temp-imports", fileName);
            } catch (Exception ignored) {
            }
            try {
                minioService.deleteFile("imports", fileName);
            } catch (Exception ignored) {
            }
            MessageManager.error("Failed to import objects", e.getMessage());
            throw new Exception("Failed to import objects");  // to rollback db changes
            // return -1;
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public abstract int processImport(InputStream fileInputStream);

    @Transactional(rollbackOn = Exception.class)
    public abstract int validateAndImport(List<T> entities);

    public InputStream getImportHistoryFile(EntitiesImportHistory<T> history) {
        try {
            return minioService.getFile("imports", history.getFilename());
        } catch (Exception e) {
            MessageManager.error("Failed to get import history file", e.getMessage());
            return null;
        }
    }

    protected Long addImportHistory(EntitiesImportHistory<T> history, int importedCount, String fileName) {
        history.setSuccess(importedCount > 0);
        history.setEntitiesAdded(importedCount);
        history.setImportedBy(security.getUser());
        history.setImportedTime(ZonedDateTime.now());
        history.setFilename(fileName);
        em.persist(history);
        return history.getId();
    }

    protected abstract Long addImportHistory(int importedCount, String fileName);

    public abstract List<? extends EntitiesImportHistory<T>> getImportHistory();
}
