package com.example.Proyecto.controllers;

import com.example.Proyecto.entities.ReservaEntity;
import com.example.Proyecto.services.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@Controller
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    //crear reserva
    @PostMapping
    public ResponseEntity<ReservaEntity> crearReserva(
            @RequestParam Long clienteId,
            @RequestParam int cantidadVueltas,
            @RequestParam int cantidadPersonas,
            @RequestParam String fecha,
            @RequestParam boolean diaEspecial
    ) {
        ReservaEntity reserva = reservaService.crearReserva(
                clienteId,
                cantidadVueltas,
                cantidadPersonas,
                fecha,
                diaEspecial
        );
        return ResponseEntity.ok(reserva);
    }

    // Listar todas las reservas
    @GetMapping
    public ResponseEntity<List<ReservaEntity>> listarReservas() {
        return ResponseEntity.ok(reservaService.listarReservas());
    }

    // Ver una reserva por ID con detalle
    @GetMapping("/{id}")
    public ResponseEntity<String> obtenerDetalleReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerReservaDetalle(id));
    }


    @GetMapping("/{id}/enviar-comprobante")
    public ResponseEntity<String> enviarComprobantePorCorreo(@PathVariable Long id) {
        try {
            reservaService.enviarComprobantePorCorreo(id);
            return ResponseEntity.ok("Correo enviado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar correo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminarReserva(id);
        return ResponseEntity.noContent().build(); // HTTP 204 NO CONTENT
    }

    @GetMapping("/reporte-vueltas-mes")
    public ResponseEntity<Map<String, Map<String, Double>>> reporteVueltasPorMes() {
        return ResponseEntity.ok(reservaService.calcularIngresosPorVueltasPorMes());
    }

    @GetMapping("/reporte-personas-mes")
    public ResponseEntity<Map<String, Map<String, Double>>> reportePersonasPorMes() {
        return ResponseEntity.ok(reservaService.calcularIngresosPorPersonasPorMes());
    }
}
