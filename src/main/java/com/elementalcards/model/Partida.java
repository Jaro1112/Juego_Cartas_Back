package com.elementalcards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador1_id", nullable = false)
    private Usuario jugador1;

    @ManyToOne
    @JoinColumn(name = "jugador2_id", nullable = false)
    private Usuario jugador2;

    @Column(nullable = false)
    private String estado;

    @Column(name = "turno_actual", nullable = false)
    private int turnoActual;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "partida_id")
    private List<Carta> cartasJugador1;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "partida_id")
    private List<Carta> cartasJugador2;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "partida_id")
    private List<Carta> mazo;

    // Constructor, getters y setters

    public Partida() {}

    public Partida(Usuario jugador1, Usuario jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.estado = "EN_CURSO";
        this.turnoActual = 1;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getJugador1() {
        return jugador1;
    }

    public void setJugador1(Usuario jugador1) {
        this.jugador1 = jugador1;
    }

    public Usuario getJugador2() {
        return jugador2;
    }

    public void setJugador2(Usuario jugador2) {
        this.jugador2 = jugador2;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Carta> getCartasJugador1() {
        return cartasJugador1;
    }

    public void setCartasJugador1(List<Carta> cartasJugador1) {
        this.cartasJugador1 = cartasJugador1;
    }

    public List<Carta> getCartasJugador2() {
        return cartasJugador2;
    }

    public void setCartasJugador2(List<Carta> cartasJugador2) {
        this.cartasJugador2 = cartasJugador2;
    }

    public List<Carta> getMazo() {
        return mazo;
    }

    public void setMazo(List<Carta> mazo) {
        this.mazo = mazo;
    }
}
