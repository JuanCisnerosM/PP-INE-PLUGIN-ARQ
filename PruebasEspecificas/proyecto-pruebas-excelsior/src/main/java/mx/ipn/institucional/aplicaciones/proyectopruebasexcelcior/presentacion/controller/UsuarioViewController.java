package main.java.mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.controller;

import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;
import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.converter.UsuarioConverter;
import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.dto.UsuarioDTO;
import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioViewController {
    
    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;
    
    public UsuarioViewController(UsuarioService usuarioService, UsuarioConverter usuarioConverter) {
        this.usuarioService = usuarioService;
        this.usuarioConverter = usuarioConverter;
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioDTO());
        return "usuarios/formulario";
    }
    
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute UsuarioDTO usuarioDTO, Model model) {
        try {
            Usuario usuario = usuarioConverter.toEntity(usuarioDTO);
            usuario = usuarioService.guardar(usuario);
            usuarioDTO = usuarioConverter.toDTO(usuario);
            usuarioDTO.setMensaje("Usuario guardado exitosamente");
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "usuarios/detalle";
        } catch (Exception e) {
            usuarioDTO.setMensaje("Error al guardar el usuario: " + e.getMessage());
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "usuarios/formulario";
        }
    }
    
    @GetMapping("/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario != null) {
            model.addAttribute("usuarioDTO", usuarioConverter.toDTO(usuario));
            return "usuarios/detalle";
        }
        return "redirect:/usuarios/nuevo";
    }
}
