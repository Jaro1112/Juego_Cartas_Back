package com.elementalcards.repository;

import com.elementalcards.model.Carta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartaRepository extends JpaRepository<Carta, Long> {
    // Métodos personalizados si son necesarios
}
