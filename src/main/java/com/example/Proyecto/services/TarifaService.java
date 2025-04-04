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

    // Obtener todas las tarifas
    public List<TarifaEntity> obtenerTodas() {
        return tarifaRepository.findAll();
    }

    // Buscar tarifa por ID
    public Optional<TarifaEntity> buscarPorId(Long id) {
        return tarifaRepository.findById(id);
    }

    // Buscar tarifa por Vueltas
    public Optional<TarifaEntity> buscarPorVueltas(int vueltas) {
        return tarifaRepository.findByVueltas(vueltas);
    }

    // Actualizar tarifa
    public TarifaEntity actualizarTarifa(Long id, TarifaEntity actualizada) {
        return tarifaRepository.findById(id).map(tarifa -> {
            tarifa.setCosto(actualizada.getCosto());
            return tarifaRepository.save(tarifa);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));
    }

    // Eliminar tarifa
    public void eliminarTarifa(Long id) {
        if (!tarifaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada");
        }
        tarifaRepository.deleteById(id);
    }
}