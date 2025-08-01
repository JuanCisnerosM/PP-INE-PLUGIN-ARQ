package main.java.mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;

public interface UsuarioService {
    Usuario obtenerPorId(Long id);
    Usuario guardar(Usuario usuario);
    Usuario buscarPorEmail(String email);
}
