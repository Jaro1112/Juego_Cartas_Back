package com.elementalcards.service;

import com.elementalcards.model.Carta;
import com.elementalcards.repository.CartaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CartaService {

    private final CartaRepository cartaRepository;

    @Autowired
    public CartaService(CartaRepository cartaRepository) {
        this.cartaRepository = cartaRepository;
    }

    public Carta crearCarta(Carta carta) {
        return cartaRepository.save(carta);
    }

    public List<Carta> obtenerTodasLasCartas() {
        return cartaRepository.findAll();
    }

    public List<Carta> obtenerCartasAleatorias(int cantidad) {
        List<Carta> todasLasCartas = cartaRepository.findAll();
        Collections.shuffle(todasLasCartas);
        return new ArrayList<>(todasLasCartas.subList(0, Math.min(cantidad, todasLasCartas.size())));
    }
}
