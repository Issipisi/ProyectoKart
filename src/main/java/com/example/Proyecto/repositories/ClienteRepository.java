package com.example.Proyecto.repositories;
import com.example.Proyecto.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    //Buscar por nombre
    //Optional<ClienteEntity> findByNombre(String nombre);

    //Buscar por email
    //Optional<ClienteEntity> findByEmail(String email);


}
