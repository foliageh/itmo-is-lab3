package com.twillice.itmoislab1.service;

import com.twillice.itmoislab1.model.User;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class UserService {
    @PersistenceContext
    private EntityManager em;

    public User find(Long id) {
        return em.find(User.class, id);
    }

    public User find(String username) {
        return em.createQuery("from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream().findAny().orElse(null);
    }

    public User find(String username, String password) {
        return em.createQuery("from User u where u.username = :username and u.password = :password", User.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getResultStream().findAny().orElse(null);
    }

    public List<User> list() {
        return em.createQuery("from User", User.class).getResultList();
    }

    public Long create(User user) {
        em.persist(user);
        return user.getId();
    }

    public void update(User user) {
        em.merge(user);
    }
}
