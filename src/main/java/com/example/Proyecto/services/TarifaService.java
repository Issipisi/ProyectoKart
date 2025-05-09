package com.example.Proyecto.services;

import com.example.Proyecto.entities.TarifaEntity;
import com.example.Proyecto.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;

@Service
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    @Autowired
    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    // Crear una nueva tarifa
    // Busca o crea tarifa según vueltas y si es día especial
    public TarifaEntity buscarOCrearTarifa(int vueltas, boolean diaEspecial) {
        int minutos = 0;
        double costo = 0;

        switch (vueltas) {
            case 10:
                minutos = 30;
                costo = diaEspecial ? 18000 : 15000;
                break;
            case 15:
                minutos = 35;
                costo = diaEspecial ? 23000 : 20000;
                break;
            case 20:
                minutos = 40;
                costo = diaEspecial ? 28000 : 25000;
                break;
            default:
                throw new IllegalArgumentException("Número de vueltas inválido");
        }

        Optional<TarifaEntity> tarifaExistente = tarifaRepository.findByVueltasAndCostoAndDuracion(
                vueltas, costo, minutos
        );

        int finalMinutos = minutos;
        double finalCosto = costo;

        return tarifaExistente.orElseGet(() -> tarifaRepository.save(
                TarifaEntity.builder()
                        .vueltas(vueltas)
                        .duracion(finalMinutos)
                        .costo(finalCosto)
                        .build()
        ));
    }

    // Buscar tarifa por Vueltas
    public Optional<TarifaEntity> buscarPorVueltas(int vueltas) {
        return tarifaRepository.findByVueltas(vueltas);
    }

    // Obtener todas las tarifas
    public List<TarifaEntity> obtenerTodas() {
        return tarifaRepository.findAll();
    }

}