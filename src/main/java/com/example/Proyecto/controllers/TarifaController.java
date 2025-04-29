package com.example.Proyecto.controllers;

import com.example.Proyecto.entities.TarifaEntity;
import com.example.Proyecto.services.TarifaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://40.82.176.155"
})
@RequestMapping("/api/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    // Crear nueva tarifa
    @PostMapping
    public ResponseEntity<TarifaEntity> crearTarifa(@RequestBody TarifaEntity tarifa) {
        TarifaEntity nueva = tarifaService.crearTarifa(tarifa);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    // Buscar tarifa por cantidad de vueltas
    @GetMapping("/buscarPorVueltas")
    public ResponseEntity<TarifaEntity> buscarPorVueltas(@RequestParam int vueltas) {
        Optional<TarifaEntity> tarifa = tarifaService.buscarPorVueltas(vueltas);
        return tarifa.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar todas las tarifas
    @GetMapping
    public ResponseEntity<List<TarifaEntity>> listarTodas() {
        return ResponseEntity.ok(tarifaService.obtenerTodas());
    }

    /*
    @GetMapping("/tarifas/activas")
    public List<TarifaEntity> obtenerTarifasPorTipo(@RequestParam boolean esEspecial)
    */

    /* Buscar tarifa por ID
    @GetMapping("/{id}")
    public ResponseEntity<TarifaEntity> buscarPorId(@PathVariable Long id) {
        Optional<TarifaEntity> tarifa = tarifaService.buscarPorId(id);
        return tarifa.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar tarifa existente
    @PutMapping("/{id}")
    public ResponseEntity<TarifaEntity> actualizarTarifa(@PathVariable Long id, @RequestBody TarifaEntity actualizada) {
        try {
            TarifaEntity tarifa = tarifaService.actualizarTarifa(id, actualizada);
            return ResponseEntity.ok(tarifa);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Eliminar tarifa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
        tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }*/
}