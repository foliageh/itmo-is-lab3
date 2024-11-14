package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.Chapter;
import com.twillice.itmoislab1.security.Security;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.ZonedDateTime;
import java.util.List;

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

    public Long create(Chapter chapter) {
        chapter.setCreatedBy(security.getUser());
        chapter.setCreatedTime(ZonedDateTime.now());
        em.persist(chapter);
        return chapter.getId();
    }


    public void update(Chapter chapter) {
        chapter.setUpdateFields(security.getUser(), ZonedDateTime.now());
        em.merge(chapter);
    }

    public void remove(Chapter chapter) {
        em.remove(em.contains(chapter) ? chapter : em.merge(chapter));
    }
}
