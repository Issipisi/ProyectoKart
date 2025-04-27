package com.example.Proyecto.repositories;

import com.example.Proyecto.entities.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<TarifaEntity, Long> {
    //Buscar los datos de una tarifa por cantidad de vueltas
    Optional<TarifaEntity> findByVueltas(int vueltas);

    Optional<TarifaEntity> findByVueltasAndCostoAndDuracion(int vueltas, double costo, int duracion);
}
