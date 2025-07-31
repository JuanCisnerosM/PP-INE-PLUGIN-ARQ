package com.miempresa.pruebasespecificasdecapa.service.impl;

import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import com.miempresa.pruebasespecificasdecapa.repository.UsuarioRepository;
import com.miempresa.pruebasespecificasdecapa.service.UsuarioService;
import java.util.List;

public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public void delete(Long id) {
        usuarioRepository.delete(id);
    }
}
