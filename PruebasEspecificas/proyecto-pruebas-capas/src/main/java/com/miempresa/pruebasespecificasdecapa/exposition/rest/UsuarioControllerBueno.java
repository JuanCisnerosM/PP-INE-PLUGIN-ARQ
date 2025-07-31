package com.miempresa.pruebasespecificasdecapa.exposition.rest;

import com.miempresa.pruebasespecificasdecapa.exposition.dto.UsuarioDTO;
import com.miempresa.pruebasespecificasdecapa.service.UsuarioService;
import com.miempresa.pruebasespecificasdecapa.domain.Usuario;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioControllerBueno {
    private final UsuarioService usuarioService;  // Correcto: usa la interfaz del servicio

    public UsuarioControllerBueno(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public UsuarioDTO findById(Long id) {  // Correcto: retorna DTO
        Usuario usuario = usuarioService.findById(id);
        return convertToDTO(usuario);
    }

    public List<UsuarioDTO> findAll() {  // Correcto: retorna Lista de DTOs
        return usuarioService.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO save(UsuarioDTO usuarioDTO) {  // Correcto: recibe DTO
        Usuario usuario = convertToEntity(usuarioDTO);
        Usuario savedUsuario = usuarioService.save(usuario);
        return convertToDTO(savedUsuario);
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail()
        );
    }

    private Usuario convertToEntity(UsuarioDTO dto) {
        if (dto == null) return null;
        return new Usuario(
            dto.getId(),
            dto.getNombre(),
            dto.getEmail()
        );
    }
}
