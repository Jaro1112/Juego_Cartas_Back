package com.elementalcards.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.elementalcards.model.Partida;
import com.elementalcards.model.Usuario;
import com.elementalcards.service.JuegoService;
import com.elementalcards.service.UsuarioService;

import java.util.Map;

@CrossOrigin(origins = "https://juego-cartas-front.vercel.app")
@RestController
@RequestMapping("/api/juego")
public class JuegoController {

    private final JuegoService juegoService;
    private final UsuarioService usuarioService;

    @Autowired
    public JuegoController(JuegoService juegoService, UsuarioService usuarioService) {
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/iniciar")
public ResponseEntity<?> iniciarPartida(@RequestBody Map<String, Long> datos) {
    try {
        Long jugadorId = datos.get("jugadorId");
        System.out.println("Recibido jugadorId: " + jugadorId);
        if (jugadorId == null) {
            return ResponseEntity.badRequest().body("El ID del jugador es requerido");
        }
        Usuario jugador1 = usuarioService.obtenerUsuarioPorId(jugadorId);
        System.out.println("Jugador1 encontrado: " + (jugador1 != null ? jugador1.getUsername() + ", ID: " + jugador1.getId() : "null"));
        if (jugador1 == null) {
            return ResponseEntity.badRequest().body("El jugador no existe");
        }
        
        Usuario jugador2 = usuarioService.buscarOponenteConTimeout(jugadorId, 30);
        if (jugador2 == null) {
            jugador2 = usuarioService.crearUsuarioBot();
            System.out.println("Oponente bot creado: " + jugador2.getUsername() + ", ID: " + jugador2.getId());
            jugador2 = usuarioService.guardarUsuario(jugador2);
            System.out.println("Oponente bot guardado: " + jugador2.getUsername() + ", ID: " + jugador2.getId());
        } else {
            System.out.println("Jugador2 encontrado: " + jugador2.getUsername() + ", ID: " + jugador2.getId());
        }
        
        System.out.println("Iniciando partida con jugador1 ID: " + jugador1.getId() + " y jugador2 ID: " + jugador2.getId());
        Partida nuevaPartida = juegoService.iniciarPartida(jugador1, jugador2);
        System.out.println("Nueva partida creada con ID: " + nuevaPartida.getId());
        return ResponseEntity.ok(nuevaPartida);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar la partida: " + e.getMessage());
    }
}

    @PostMapping("/jugar-carta")
    public ResponseEntity<?> jugarCarta(@RequestBody Map<String, Long> datos) {
        juegoService.jugarCarta(datos.get("partidaId"), datos.get("jugadorId"), datos.get("cartaId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/robar-carta")
    public ResponseEntity<Partida> robarCarta(@RequestBody Map<String, Long> datos) {
        Partida partidaActualizada = juegoService.robarCarta(datos.get("partidaId"), datos.get("jugadorId"));
        return ResponseEntity.ok(partidaActualizada);
    }

    @GetMapping("/estado/{partidaId}")
    public ResponseEntity<Partida> obtenerEstadoPartida(@PathVariable Long partidaId) {
        Partida partida = juegoService.obtenerPartida(partidaId);
        return ResponseEntity.ok(partida);
    }

    @PostMapping("/simular/{partidaId}")
    public ResponseEntity<?> simularPartida(@PathVariable Long partidaId) {
        juegoService.simularPartida(partidaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Bienvenido a la API del Juego de Cartas Elemental");
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearOObtenerUsuario(@RequestBody Map<String, String> datos) {
        try {
            String username = datos.get("username");
            System.out.println("Intentando crear o obtener usuario: " + username);
            Usuario usuario = usuarioService.crearOObtenerUsuario(username);
            System.out.println("Usuario creado/obtenido: " + usuario.getId() + " - " + usuario.getUsername());
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            System.err.println("Error al crear o obtener usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear o obtener usuario: " + e.getMessage());
        }
    }

    @PostMapping("/rendirse")
    public ResponseEntity<?> rendirse(@RequestBody Map<String, Long> datos) {
        try {
            juegoService.rendirse(datos.get("partidaId"), datos.get("jugadorId"));
            return ResponseEntity.ok().body("{\"message\": \"Rendición exitosa\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Error al rendirse: " + e.getMessage() + "\"}");
        }
    }
}
