package com.example.Proyecto.repositories;

import com.example.Proyecto.entities.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {
    //Rack semanal, para buscar reservas entre una fecha
    List<ReservaEntity> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

}
