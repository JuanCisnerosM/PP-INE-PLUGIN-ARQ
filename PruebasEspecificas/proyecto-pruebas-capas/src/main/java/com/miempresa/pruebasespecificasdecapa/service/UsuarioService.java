package com.miempresa.pruebasespecificasdecapa.service;

import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import java.util.List;

public interface UsuarioService {
    Usuario findById(Long id);
    List<Usuario> findAll();
    Usuario save(Usuario usuario);
    void delete(Long id);
}
