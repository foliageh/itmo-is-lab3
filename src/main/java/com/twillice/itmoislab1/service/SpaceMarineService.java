package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.model.SpaceMarine;
import com.twillice.itmoislab1.security.Security;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.ZonedDateTime;
import java.util.List;

@Stateless
public class SpaceMarineService {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Security security;

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

    public Long create(SpaceMarine spaceMarine) {
        spaceMarine.setCreatedBy(security.getUser());
        spaceMarine.setCreatedTime(ZonedDateTime.now());
        em.persist(spaceMarine);
        return spaceMarine.getId();
    }

    public void update(SpaceMarine spaceMarine) {
        spaceMarine.setUpdateFields(security.getUser(), ZonedDateTime.now());
        em.merge(spaceMarine);
    }

    public void remove(SpaceMarine spaceMarine) {
        em.remove(em.contains(spaceMarine) ? spaceMarine : em.merge(spaceMarine));
    }
}
