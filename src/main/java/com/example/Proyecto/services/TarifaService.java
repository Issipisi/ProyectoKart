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
    public TarifaEntity crearTarifa(TarifaEntity tarifa) {
        return tarifaRepository.save(tarifa);
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