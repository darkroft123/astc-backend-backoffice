package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;

@ApplicationScoped
public class RoleRepository {

    @PersistenceContext
    EntityManager em;

    public RoleEntity findById(UUID id) {
        return em.find(RoleEntity.class, id);
    }
}