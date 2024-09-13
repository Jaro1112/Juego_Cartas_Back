package com.elementalcards.model;

public class PartidaWebSocket {
    private Long id;
    private String tipo;
    private Long jugadorRendidoId;

    public PartidaWebSocket(Partida partida, String tipo, Long jugadorRendidoId) {
        this.id = partida.getId();
        this.tipo = tipo;
        this.jugadorRendidoId = jugadorRendidoId;
    }
    // Getters
    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public Long getJugadorRendidoId() {
        return jugadorRendidoId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setJugadorRendidoId(Long jugadorRendidoId) {
        this.jugadorRendidoId = jugadorRendidoId;
    }

    
}