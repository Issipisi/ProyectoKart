package com.example.Proyecto.repositories;

import com.example.Proyecto.entities.KartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KartRepository extends JpaRepository<KartEntity, Long> {

    /*Buscar Kart por codigo
    Optional<KartEntity> findByCodigo(String codigo);*/
}
