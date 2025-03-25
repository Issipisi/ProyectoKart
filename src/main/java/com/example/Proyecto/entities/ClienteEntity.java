package com.example.Proyecto.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Table(name = "cliente")

public class ClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "nombre")
    String nombre;

    @Column(name = "email")
    String email;

    //Para aplicar los descuentos a clientes frecuentes x mes
    @Column(name = "totalReservas")
    int totalReservas;




}


