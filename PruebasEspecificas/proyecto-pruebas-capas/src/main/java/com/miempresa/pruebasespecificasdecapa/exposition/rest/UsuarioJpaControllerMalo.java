package com.miempresa.pruebasespecificasdecapa.exposition.rest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import com.miempresa.pruebasespecificasdecapa.repository.entity.UsuarioEntity;

/**
 * Este controlador viola la regla: NoSqlOrJpaInControllerRule
 * Demuestra el uso incorrecto de JPA directamente en la capa de exposición
 */
public class UsuarioJpaControllerMalo {
    private final EntityManager entityManager;

    public UsuarioJpaControllerMalo(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<?> findAllUsuarios() {
        // Viola la regla al usar SQL directo en el controlador
        Query query = entityManager.createQuery("SELECT u FROM UsuarioEntity u");
        return query.getResultList();
    }

    public void deleteUsuario(Long id) {
        // Viola la regla al usar JPA directamente en el controlador
        entityManager.remove(entityManager.find(UsuarioEntity.class, id));
    }
}
