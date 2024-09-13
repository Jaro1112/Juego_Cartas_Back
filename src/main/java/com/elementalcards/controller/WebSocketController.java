package com.elementalcards.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import com.elementalcards.service.UsuarioService;
import com.elementalcards.model.Usuario;
import com.elementalcards.model.Partida;
import com.elementalcards.service.JuegoService;

@Controller
public class WebSocketController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JuegoService juegoService;

    @MessageMapping("/buscarOponente")
    @SendTo("/topic/emparejamiento")
    public Partida buscarOponente(Long jugadorId) throws Exception {
        Usuario jugador = usuarioService.obtenerUsuarioPorId(jugadorId);
        Usuario oponente = usuarioService.buscarOponenteConTimeout(jugadorId, 30);
        
        if (oponente != null) {
            return juegoService.iniciarPartida(jugador, oponente);
        }
        
        return null;
    }
}