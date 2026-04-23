package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

    @PersistenceContext
    EntityManager em;

    public void save(UserEntity user) {
        em.merge(user);
    }

    public UserEntity findById(UUID id) {
        return em.find(UserEntity.class, id);
    }

    public List<UserEntity> findAll() {
        return em.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                .getResultList();
    }

    public boolean delete(UUID id) {
        UserEntity user = findById(id);
        if (user == null) return false;

        em.remove(em.contains(user) ? user : em.merge(user));
        return true;
    }

    public UserEntity findByEmail(String email) {
        return em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.email = :email",
                UserEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}