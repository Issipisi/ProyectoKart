package com.example.Proyecto.repositories;

import com.example.Proyecto.entities.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<TarifaEntity, Long> {

}
