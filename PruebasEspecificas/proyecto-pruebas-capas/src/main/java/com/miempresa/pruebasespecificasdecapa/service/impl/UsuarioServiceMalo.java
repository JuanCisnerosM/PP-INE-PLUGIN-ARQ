package com.miempresa.pruebasespecificasdecapa.service.impl;

import com.miempresa.pruebasespecificasdecapa.exposition.rest.UsuarioControllerBueno;
import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import com.miempresa.pruebasespecificasdecapa.service.UsuarioService;
import java.util.List;

/**
 * Este servicio viola la regla: ServiceShouldNotDependOnControllerRule
 * Demuestra una dependencia incorrecta de la capa de servicio hacia la capa de exposición
 */
public class UsuarioServiceMalo implements UsuarioService {
    private final UsuarioControllerBueno controller; // Viola ServiceShouldNotDependOnControllerRule

    public UsuarioServiceMalo(UsuarioControllerBueno controller) {
        this.controller = controller;
    }

    @Override
    public Usuario findById(Long id) {
        // Viola el principio al depender del controlador
        return null; // Implementación simulada
    }

    @Override
    public List<Usuario> findAll() {
        // Viola el principio al depender del controlador
        return null; // Implementación simulada
    }

    @Override
    public Usuario save(Usuario usuario) {
        // Viola el principio al depender del controlador
        return null; // Implementación simulada
    }

    @Override
    public void delete(Long id) {
        // Viola el principio al depender del controlador
    }
}
