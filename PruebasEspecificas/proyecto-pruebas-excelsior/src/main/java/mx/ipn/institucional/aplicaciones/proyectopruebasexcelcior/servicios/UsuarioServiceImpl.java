package main.java.mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;
import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioServiceImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return repository.save(usuario);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }
}
