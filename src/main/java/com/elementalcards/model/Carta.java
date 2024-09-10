package com.elementalcards.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cartas")
public class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Elemento elemento;

    @Column(nullable = false)
    private int poder;

    @Column(nullable = false)
    private String debilidad;

    @Column
    private String efecto;

    public enum Elemento {
        FUEGO, AGUA, TIERRA, AIRE, RAYO
    }

    // Constructor
    public Carta() {}

    public Carta(Elemento elemento, int poder, String debilidad, String efecto) {
        this.elemento = elemento;
        this.poder = poder;
        this.debilidad = debilidad;
        this.efecto = efecto;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Elemento getElemento() {
        return elemento;
    }

    public void setElemento(Elemento elemento) {
        this.elemento = elemento;
    }

    public int getPoder() {
        return poder;
    }

    public void setPoder(int poder) {
        this.poder = poder;
    }

    public String getDebilidad() {
        return debilidad;
    }

    public void setDebilidad(String debilidad) {
        this.debilidad = debilidad;
    }

    public String getEfecto() {
        return efecto;
    }

    public void setEfecto(String efecto) {
        this.efecto = efecto;
    }

    public int compararCon(Carta otraCarta) {
        if (this.elemento == otraCarta.elemento) {
            return Integer.compare(this.poder, otraCarta.poder);
        }

        switch (this.elemento) {
            case FUEGO:
                if (otraCarta.elemento == Elemento.AGUA) return -1;
                if (otraCarta.elemento == Elemento.AIRE) return 1;
                break;
            case AGUA:
                if (otraCarta.elemento == Elemento.TIERRA) return -1;
                if (otraCarta.elemento == Elemento.FUEGO) return 1;
                break;
            case TIERRA:
                if (otraCarta.elemento == Elemento.AIRE) return -1;
                if (otraCarta.elemento == Elemento.AGUA) return 1;
                break;
            case AIRE:
                if (otraCarta.elemento == Elemento.FUEGO) return -1;
                if (otraCarta.elemento == Elemento.TIERRA) return 1;
                break;
            case RAYO:
                if (otraCarta.elemento == Elemento.TIERRA) return -1;
                if (otraCarta.elemento == Elemento.AGUA) return 1;
                break;
        }

        return 0; // Si no hay ventaja elemental, se considera un empate
    }
}
