package com.example.Proyecto.repositories;
import com.example.Proyecto.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
    //@Query(value = "SELECT * FROM cliente WHERE ")
}
