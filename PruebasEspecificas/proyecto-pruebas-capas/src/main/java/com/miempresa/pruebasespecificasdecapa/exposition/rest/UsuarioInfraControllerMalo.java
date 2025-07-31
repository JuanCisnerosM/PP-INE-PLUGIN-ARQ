package com.miempresa.pruebasespecificasdecapa.exposition.rest;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

/**
 * Este controlador viola las reglas:
 * - NoInfrastructureDependencyFromExpositionRule
 * - NoDirectAccessToRepoModelInfraRule
 */
public class UsuarioInfraControllerMalo {
    private final JdbcTemplate jdbcTemplate;

    public UsuarioInfraControllerMalo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAllUsuarios() {
        // Viola la regla al acceder directamente a la infraestructura
        return jdbcTemplate.queryForList("SELECT * FROM usuarios");
    }

    public void updateUsuario(Long id, String nombre) {
        // Viola la regla al ejecutar SQL directamente desde el controlador
        jdbcTemplate.update(
            "UPDATE usuarios SET nombre = ? WHERE id = ?",
            nombre, id
        );
    }
}
