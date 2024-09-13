package com.elementalcards.service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.elementalcards.model.Usuario;
import com.elementalcards.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConcurrentLinkedQueue<QueueEntry> waitingQueue = new ConcurrentLinkedQueue<>();
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

    public Usuario buscarOponenteConTimeout(Long jugadorId, int timeoutSeconds) {
        CompletableFuture<Usuario> future = new CompletableFuture<>();
        QueueEntry entry = new QueueEntry(jugadorId, future);
        waitingQueue.offer(entry);

        while (!future.isDone()) {
            QueueEntry polledEntry = waitingQueue.poll();
            if (polledEntry != null && !polledEntry.getFuture().isDone() && !polledEntry.getJugadorId().equals(jugadorId)) {
                Usuario jugador = usuarioRepository.findById(jugadorId).orElseThrow();
                polledEntry.getFuture().complete(jugador);
                return usuarioRepository.findById(polledEntry.getJugadorId()).orElseThrow();
            } else if (polledEntry != null) {
                waitingQueue.offer(polledEntry);
            }
        }

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            waitingQueue.removeIf(queueEntry -> queueEntry.getJugadorId().equals(jugadorId));
            return null;
        }
    }

    public Usuario crearUsuarioBot() {
        String botUsername = "Bot_" + System.currentTimeMillis();
        Usuario bot = new Usuario();
        bot.setUsername(botUsername);
        bot.setEmail(botUsername + "@bot.com");
        bot.setPassword("bot_password");
        return usuarioRepository.save(bot);
    }

    // Otros métodos...

    private static class QueueEntry {
        private final Long jugadorId;
        private final CompletableFuture<Usuario> future;
    
        public QueueEntry(Long jugadorId, CompletableFuture<Usuario> future) {
            this.jugadorId = jugadorId;
            this.future = future;
        }
    
        public Long getJugadorId() {
            return jugadorId;
        }
    
        public CompletableFuture<Usuario> getFuture() {
            return future;
        }
    }
}




