package com.elementalcards.service;

import com.elementalcards.model.Usuario;
import com.elementalcards.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;
import java.lang.Thread;
import java.lang.InterruptedException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(String username, String email, String password) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setVida(20);
        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String password) {
        return usuarioRepository.findByEmail(email)
            .filter(usuario -> passwordEncoder.matches(password, usuario.getPassword()))
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public Usuario crearOObtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseGet(() -> {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setUsername(username);
                nuevoUsuario.setVida(20);
                return usuarioRepository.save(nuevoUsuario);
            });
    }

    public Usuario obtenerOponenteAleatorio(Long jugadorId) {
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        List<Usuario> posiblesOponentes = todosLosUsuarios.stream()
                .filter(u -> !u.getId().equals(jugadorId))
                .collect(Collectors.toList());
        if (posiblesOponentes.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        return posiblesOponentes.get(rand.nextInt(posiblesOponentes.size()));
    }

    public Usuario crearUsuarioBot() {
        String botUsername = "Bot_" + System.currentTimeMillis();
        Usuario bot = new Usuario();
        bot.setUsername(botUsername);
        bot.setEmail(botUsername + "@bot.com");
        bot.setPassword("bot_password");
        bot.setVida(20);
        return usuarioRepository.save(bot);
    }

    public Usuario buscarOponenteConTimeout(Long jugadorId, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutSeconds * 1000);
        
        while (System.currentTimeMillis() < endTime) {
            Usuario oponente = obtenerOponenteAleatorio(jugadorId);
            if (oponente != null) {
                return oponente;
            }
            try {
                Thread.sleep(1000); // Esperar 1 segundo antes de intentar de nuevo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null; // Si no se encuentra un oponente después del timeout
    }
}


