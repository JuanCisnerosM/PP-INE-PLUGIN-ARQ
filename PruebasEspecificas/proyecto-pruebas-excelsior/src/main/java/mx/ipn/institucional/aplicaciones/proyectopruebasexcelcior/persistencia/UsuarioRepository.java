package main.java.mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia;

import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}
