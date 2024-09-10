package com.elementalcards.repository;

import com.elementalcards.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    // Métodos personalizados si son necesarios
}
