package com.elementalcards.service;

import com.elementalcards.model.Carta;
import com.elementalcards.model.Partida;
import com.elementalcards.model.Usuario;
import com.elementalcards.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class JuegoService {

    private final PartidaRepository partidaRepository;
    private final CartaService cartaService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public JuegoService(PartidaRepository partidaRepository, CartaService cartaService, SimpMessagingTemplate messagingTemplate) {
        this.partidaRepository = partidaRepository;
        this.cartaService = cartaService;
        this.messagingTemplate = messagingTemplate;
    }

    public Partida iniciarPartida(Usuario jugador1, Usuario jugador2) {
        Partida nuevaPartida = new Partida(jugador1, jugador2);
        
        // Repartir 5 cartas aleatorias a cada jugador
        List<Carta> cartasJugador1 = cartaService.obtenerCartasAleatorias(5);
        List<Carta> cartasJugador2 = cartaService.obtenerCartasAleatorias(5);
        
        nuevaPartida.setCartasJugador1(cartasJugador1);
        nuevaPartida.setCartasJugador2(cartasJugador2);

        // Crear el mazo principal
        List<Carta> mazo = cartaService.obtenerCartasAleatorias(40); // Por ejemplo, 40 cartas en el mazo
        nuevaPartida.setMazo(mazo);

        return partidaRepository.save(nuevaPartida);
    }

    public void jugarCarta(Long partidaId, Long jugadorId, Long cartaId) {
        Partida partida = partidaRepository.findById(partidaId).orElseThrow();
        Usuario jugadorActual = partida.getTurnoActual() == 1 ? partida.getJugador1() : partida.getJugador2();
        
        if (!jugadorActual.getId().equals(jugadorId)) {
            throw new IllegalStateException("No es el turno de este jugador");
        }

        List<Carta> manoJugador = partida.getTurnoActual() == 1 ? partida.getCartasJugador1() : partida.getCartasJugador2();
        Carta cartaJugada = manoJugador.stream()
                .filter(c -> c.getId().equals(cartaId))
                .findFirst()
                .orElseThrow();
        
        manoJugador.remove(cartaJugada);

        // Procesar la carta jugada y resolver el combate
        resolverCombate(partida, cartaJugada);

        // Cambiar el turno
        cambiarTurno(partida);

        // Guardar los cambios en la partida
        partidaRepository.save(partida);

        // Notificar a los jugadores sobre la carta jugada y el resultado
        messagingTemplate.convertAndSend("/topic/partida/" + partidaId, "Carta jugada: " + cartaJugada.getElemento());
    }

    public Partida robarCarta(Long partidaId, Long jugadorId) {
        Partida partida = partidaRepository.findById(partidaId).orElseThrow();
        Usuario jugadorActual = partida.getTurnoActual() == 1 ? partida.getJugador1() : partida.getJugador2();
        
        if (!jugadorActual.getId().equals(jugadorId)) {
            throw new IllegalStateException("No es el turno de este jugador");
        }

        List<Carta> manoJugador = partida.getTurnoActual() == 1 ? partida.getCartasJugador1() : partida.getCartasJugador2();
        List<Carta> mazo = partida.getMazo();

        if (manoJugador.size() < 5 && !mazo.isEmpty()) {
            Carta cartaRobada = mazo.remove(0);
            manoJugador.add(cartaRobada);
            
            // Cambiar el turno solo si el jugador tenía menos de 5 cartas
            if (manoJugador.size() > 1) {
                cambiarTurno(partida);
            }

            partida = partidaRepository.save(partida);
            messagingTemplate.convertAndSend("/topic/partida/" + partidaId, 
                jugadorActual.getUsername() + " ha robado una carta. Cartas en mano: " + manoJugador.size());
        } else if (mazo.isEmpty()) {
            // Si el mazo está vacío, repartir 5 nuevas cartas
            List<Carta> nuevasCartas = cartaService.obtenerCartasAleatorias(5);
            manoJugador.addAll(nuevasCartas);
            
            partida = partidaRepository.save(partida);
            messagingTemplate.convertAndSend("/topic/partida/" + partidaId, 
                jugadorActual.getUsername() + " ha recibido 5 nuevas cartas. Cartas en mano: " + manoJugador.size());
        }

        return partida;
    }


    private void cambiarTurno(Partida partida) {
        partida.setTurnoActual(partida.getTurnoActual() == 1 ? 2 : 1);
        
        // Verificar si el jugador que ahora tiene el turno necesita robar cartas
        List<Carta> manoJugadorActual = partida.getTurnoActual() == 1 ? partida.getCartasJugador1() : partida.getCartasJugador2();
        if (manoJugadorActual.size() < 5) {
            robarCarta(partida.getId(), partida.getTurnoActual() == 1 ? partida.getJugador1().getId() : partida.getJugador2().getId());
        }
        
        messagingTemplate.convertAndSend("/topic/partida/" + partida.getId(), 
            "Turno cambiado. Ahora es el turno del jugador " + partida.getTurnoActual());
    }

    private void resolverCombate(Partida partida, Carta cartaJugada) {
        Usuario jugadorActual = partida.getTurnoActual() == 1 ? partida.getJugador1() : partida.getJugador2();
        Usuario jugadorOponente = partida.getTurnoActual() == 1 ? partida.getJugador2() : partida.getJugador1();
        
        List<Carta> manoOponente = partida.getTurnoActual() == 1 ? partida.getCartasJugador2() : partida.getCartasJugador1();
        Carta cartaOponente = seleccionarCartaAleatoria(manoOponente);
        manoOponente.remove(cartaOponente);

        int resultadoComparacion = cartaJugada.compararCon(cartaOponente);
        int danio = 0;

        if (resultadoComparacion > 0) {
            danio = cartaJugada.getPoder() - cartaOponente.getPoder();
            jugadorOponente.setVida(jugadorOponente.getVida() - danio);
            aplicarEfectoEspecial(cartaJugada, jugadorActual, jugadorOponente);
        } else if (resultadoComparacion < 0) {
            danio = cartaOponente.getPoder() - cartaJugada.getPoder();
            jugadorActual.setVida(jugadorActual.getVida() - danio);
            aplicarEfectoEspecial(cartaOponente, jugadorOponente, jugadorActual);
        } else {
            // Empate, no se hace daño
        }

        // Verificar condiciones de victoria/derrota
        if (jugadorActual.getVida() <= 0 || jugadorOponente.getVida() <= 0) {
            finalizarPartida(partida);
        }

        // Guardar los cambios en la partida
        partidaRepository.save(partida);

        // Notificar a los jugadores sobre el resultado del combate
        messagingTemplate.convertAndSend("/topic/partida/" + partida.getId(), 
            "Combate: " + jugadorActual.getUsername() + " (" + cartaJugada.getElemento() + ") vs " +
            jugadorOponente.getUsername() + " (" + cartaOponente.getElemento() + "). Daño: " + danio);
    }

    private void aplicarEfectoEspecial(Carta carta, Usuario jugadorCarta, Usuario jugadorOponente) {
        if (carta.getEfecto() != null) {
            switch (carta.getEfecto()) {
                case "CURAR":
                    jugadorCarta.setVida(Math.min(jugadorCarta.getVida() + 2, 20));
                    messagingTemplate.convertAndSend("/topic/partida/" + jugadorCarta.getId(), 
                        jugadorCarta.getUsername() + " se ha curado 2 puntos de vida.");
                    break;
                case "QUEMAR":
                    jugadorOponente.setVida(jugadorOponente.getVida() - 1);
                    messagingTemplate.convertAndSend("/topic/partida/" + jugadorOponente.getId(), 
                        jugadorOponente.getUsername() + " ha recibido 1 punto de daño por quemadura.");
                    break;
                case "ROBAR_CARTA":
                    // Implementar lógica para robar una carta adicional
                    break;
                // Añadir más efectos según sea necesario
            }
        }
    }

    private void finalizarPartida(Partida partida) {
        Usuario ganador = partida.getJugador1().getVida() > 0 ? partida.getJugador1() : partida.getJugador2();
        Usuario perdedor = ganador == partida.getJugador1() ? partida.getJugador2() : partida.getJugador1();

        partida.setEstado("TERMINADO");
        partidaRepository.save(partida);

        // Notificar a los jugadores sobre el fin del juego
        messagingTemplate.convertAndSend("/topic/partida/" + partida.getId(), 
            "La partida ha terminado. Ganador: " + ganador.getUsername() + 
            ". Perdedor: " + perdedor.getUsername());

        // Reiniciar a los jugadores para una nueva partida
        reiniciarJugador(ganador);
        reiniciarJugador(perdedor);
    }

    private void reiniciarJugador(Usuario jugador) {
        jugador.setVida(20); // Reiniciar la vida a 20
        // Si hay otros atributos que necesiten reiniciarse, hazlo aquí
        // Por ejemplo: jugador.setMano(new ArrayList<>());
    }

    // Método para iniciar una nueva partida después de que una ha terminado
    public Partida iniciarNuevaPartida(Usuario jugador1, Usuario jugador2) {
        reiniciarJugador(jugador1);
        reiniciarJugador(jugador2);
        return iniciarPartida(jugador1, jugador2);
    }

    private Carta seleccionarCartaAleatoria(List<Carta> mano) {
        Random random = new Random();
        int indiceAleatorio = random.nextInt(mano.size());
        return mano.get(indiceAleatorio);
    }

    public void simularPartida(Long partidaId) {
        Partida partida = partidaRepository.findById(partidaId).orElseThrow();
        
        while (!"TERMINADO".equals(partida.getEstado())) {
            Usuario jugadorActual = partida.getTurnoActual() == 1 ? partida.getJugador1() : partida.getJugador2();
            List<Carta> manoJugador = partida.getTurnoActual() == 1 ? partida.getCartasJugador1() : partida.getCartasJugador2();
            
            // Robar carta si es necesario
            if (manoJugador.size() < 5) {
                robarCarta(partidaId, jugadorActual.getId());
            }
            
            // Jugar una carta aleatoria
            if (!manoJugador.isEmpty()) {
                Carta cartaAJugar = manoJugador.get(new Random().nextInt(manoJugador.size()));
                jugarCarta(partidaId, jugadorActual.getId(), cartaAJugar.getId());
            } else {
                // Si no hay cartas para jugar, terminar el juego
                partida.setEstado("TERMINADO");
            }
            
            // Actualizar la partida
            partida = partidaRepository.findById(partidaId).orElseThrow();
        }
        
        System.out.println("La partida ha terminado. Ganador: " + 
            (partida.getJugador1().getVida() > 0 ? partida.getJugador1().getUsername() : partida.getJugador2().getUsername()));
    }

    public Partida obtenerPartida(Long partidaId) {
        return partidaRepository.findById(partidaId)
            .orElseThrow(() -> new RuntimeException("Partida no encontrada con ID: " + partidaId));
    }

    public void rendirse(Long partidaId, Long jugadorId) {
        Partida partida = partidaRepository.findById(partidaId).orElseThrow();
        Usuario jugadorRendido = partida.getJugador1().getId().equals(jugadorId) ? partida.getJugador1() : partida.getJugador2();
        
        jugadorRendido.setVida(0);
        finalizarPartida(partida);
        
        messagingTemplate.convertAndSend("/topic/partida/" + partidaId, 
            "El jugador " + jugadorRendido.getUsername() + " se ha rendido. La partida ha terminado.");
    }
}

