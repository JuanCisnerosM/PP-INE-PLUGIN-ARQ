package main.java.mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.converter;

import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;
import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.dto.UsuarioDTO;
import org.springframework.stereotype.Component;

@Component
public class UsuarioConverter {
    
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        return dto;
    }
    
    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        return usuario;
    }
}
