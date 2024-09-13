package com.elementalcards.service;

import com.elementalcards.model.Usuario;
import com.elementalcards.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Aquí iría la lógica para validar y encriptar la contraseña
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario crearOObtenerUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setVida(20);
            usuario = usuarioRepository.save(usuario);
        }
        return usuario;
    }
}


