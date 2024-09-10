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
import com.elementalcards.model.Partida;
import com.elementalcards.model.Usuario;
import com.elementalcards.service.JuegoService;
import com.elementalcards.service.UsuarioService;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "https://juego-cartas-back.onrender.com"})
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
    public ResponseEntity<Partida> iniciarPartida(@RequestBody Map<String, Long> jugadores) {
        Usuario jugador1 = usuarioService.obtenerUsuarioPorId(jugadores.get("jugador1Id"));
        Usuario jugador2 = usuarioService.obtenerUsuarioPorId(jugadores.get("jugador2Id"));
        Partida nuevaPartida = juegoService.iniciarPartida(jugador1, jugador2);
        return ResponseEntity.ok(nuevaPartida);
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
}
