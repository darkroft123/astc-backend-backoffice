package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class OutboxEventRepository {

    @PersistenceContext
    EntityManager em;

    public void save(OutboxEventEntity event) {
        em.persist(event);
    }

    public List<OutboxEventEntity> findUnprocessed(int limit) {
        return em.createQuery(
                "SELECT e FROM OutboxEventEntity e WHERE e.processedAt IS NULL",
                OutboxEventEntity.class)
                .setMaxResults(limit)
                .getResultList();
    }
}