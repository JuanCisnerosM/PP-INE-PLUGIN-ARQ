package com.miempresa.pruebasespecificasdecapa.exposition.rest;

import com.miempresa.pruebasespecificasdecapa.service.impl.UsuarioServiceImpl;
import com.miempresa.pruebasespecificasdecapa.exposition.dto.UsuarioDTO;
import java.util.List;

/**
 * Este controlador viola la regla: ExpositionCommunicatesViaServiceInterfacesRule
 * Demuestra el uso incorrecto al depender de la implementación del servicio en lugar de su interfaz
 */
public class UsuarioControllerSinInterfaz {
    private final UsuarioServiceImpl usuarioService; // Viola la regla al usar la implementación directamente

    public UsuarioControllerSinInterfaz(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    public UsuarioDTO findById(Long id) {
        return null; // Implementación simulada
    }

    public List<UsuarioDTO> findAll() {
        return null; // Implementación simulada
    }
}
