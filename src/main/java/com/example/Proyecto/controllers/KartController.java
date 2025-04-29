package com.example.Proyecto.controllers;

import com.example.Proyecto.entities.KartEntity;
import com.example.Proyecto.services.ClienteService;
import com.example.Proyecto.services.KartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://40.82.176.155"
})
@RestController
@RequestMapping("/api/karts")
public class KartController {

    private final KartService kartService;

    public KartController(KartService kartService) {
        this.kartService = kartService;
    }

    // Crear nuevo kart
    @PostMapping
    public ResponseEntity<KartEntity> registrarKart(@RequestBody KartEntity kart) {
        KartEntity kartCreado = kartService.registrarKart(kart);
        return new ResponseEntity<>(kartCreado, HttpStatus.CREATED);
    }

    // Listar todos los kart
    @GetMapping
    public ResponseEntity<List<KartEntity>> listarKarts() {
        List<KartEntity> karts = kartService.obtenerTodosLosKart();
        return ResponseEntity.ok(karts);
    }



}
