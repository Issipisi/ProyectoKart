package com.example.Proyecto.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "reserva")
public class ReservaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    Long id;

    private Long clienteId;        // Relación manual
    private Long tarifaId;

    int cantidadPersonas;
    LocalDateTime fecha;    //Fecha y hora

    double montoBase;      // Tarifa seleccionada sin descuento
    double montoFinal;     // Con descuento aplicado

    String tipoDescuentoAplicado; // "GRUPO", "FRECUENTE", "ESPECIAL", "NINGUNO"
}
