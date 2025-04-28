package com.example.Proyecto.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "cliente")

public class ClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    Long id;
    String nombre;
    String email;
    //Para aplicar los descuentos a clientes frecuentes x mes
    int total_reservas;

}


