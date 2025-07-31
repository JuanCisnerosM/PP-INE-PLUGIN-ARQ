package com.miempresa.pruebasespecificasdecapa.repository;

import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import java.util.List;

public interface UsuarioRepository {
    Usuario findById(Long id);
    List<Usuario> findAll();
    Usuario save(Usuario usuario);
    void delete(Long id);
}
