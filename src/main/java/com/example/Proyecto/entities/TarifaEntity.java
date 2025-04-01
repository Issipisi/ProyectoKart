package com.example.Proyecto.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Table(name = "tarifa")

public class TarifaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    Long id;

    int costo;
    int vueltas;
    //Mediante el uso de 1 o 0 se verá si la tarifa es especial o estándar

}
