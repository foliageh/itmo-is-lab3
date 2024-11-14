package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.SpaceMarine;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class ActionsService {
    @PersistenceContext
    private EntityManager em;

    public Long calculateTotalHealth() {
        return (Long) em.createNativeQuery("SELECT calculate_total_health()", Long.class)
                .getSingleResult();
    }

    public List<SpaceMarine> getMarinesWithHealthLessThan(Long maxHealth) {
        return em.createNativeQuery("SELECT * FROM get_space_marines_with_health_less_than(:maxHealth)", SpaceMarine.class)
                .setParameter("maxHealth", maxHealth)
                .getResultList();
    }

    public List<Boolean> getUniqueLoyalValues() {
        return em.createNativeQuery("SELECT loyal FROM get_unique_loyal_values()", Boolean.class)
                .getResultList();
    }
}
