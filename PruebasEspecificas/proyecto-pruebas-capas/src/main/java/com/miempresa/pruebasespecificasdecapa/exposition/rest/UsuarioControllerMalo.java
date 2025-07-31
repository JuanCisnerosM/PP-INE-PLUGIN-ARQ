package com.miempresa.pruebasespecificasdecapa.exposition.rest;

import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import com.miempresa.pruebasespecificasdecapa.repository.UsuarioRepository;
import java.util.List;

// Este controlador viola varias reglas:
// 1. NoRepositoryAccessFromExpositionRule - Accede directamente al repositorio
// 2. NoDomainEntitiesReturnedFromExpositionRule - Retorna entidades de dominio
// 3. AvoidDomainModelInExpositionRule - Usa modelo de dominio directamente
public class UsuarioControllerMalo {
    private final UsuarioRepository usuarioRepository;  // Viola NoRepositoryAccessFromExpositionRule

    public UsuarioControllerMalo(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario findById(Long id) {  // Viola NoDomainEntitiesReturnedFromExpositionRule
        return usuarioRepository.findById(id);
    }

    public List<Usuario> findAll() {  // Viola NoDomainEntitiesReturnedFromExpositionRule
        return usuarioRepository.findAll();
    }

    public Usuario save(Usuario usuario) {  // Viola AvoidDomainModelInExpositionRule
        // Lógica de negocio directamente en el controlador - Viola NoBusinessLogicInExpositionRule
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        return usuarioRepository.save(usuario);
    }
}
