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
    @Column(name = "id")
    Long id;

    @Column(name = "costo")
    int costo;

    @Column(name = "duracion")
    int duracion;

    //Mediante el uso de 1 o 0 se verá si la tarifa es especial o estándar
    @Column(name = "esEspecial")
    int esEspecial;

}
