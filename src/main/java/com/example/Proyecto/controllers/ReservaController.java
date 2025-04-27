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
            @RequestParam Long tarifaId,
            @RequestParam int cantidadPersonas,
            @RequestParam String fecha,
            @RequestParam boolean clienteFrecuente,
            @RequestParam boolean diaEspecial
    ) {
        ReservaEntity reserva = reservaService.crearReserva(
                clienteId,
                tarifaId,
                cantidadPersonas,
                fecha,
                clienteFrecuente,
                diaEspecial
        );
        return ResponseEntity.ok(reserva);
    }

    // Listar todas las reservas
    @GetMapping
    public ResponseEntity<List<String>> listarReservasConCliente() {
        return ResponseEntity.ok(reservaService.listarReservasConCliente());
    }

    // Ver una reserva por ID con detalle
    @GetMapping("/{id}")
    public ResponseEntity<String> obtenerDetalleReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerReservaDetalle(id));
    }

    // Generar el comprobante
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<String> generarComprobante(@PathVariable Long id) {
        String comprobante = reservaService.generarComprobanteReserva(id);
        return ResponseEntity.ok(comprobante);
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

    // Reporte ingresos por vueltas
    @GetMapping("/reporte-vueltas")
    public ResponseEntity<Map<Integer, Double>> reporteIngresosPorVueltas() {
        return ResponseEntity.ok(reservaService.calcularIngresosPorVueltas());
    }

    // Reporte ingresos por cantidad de personas
    @GetMapping("/reporte-personas")
    public ResponseEntity<Map<Integer, Double>> reporteIngresosPorPersonas() {
        return ResponseEntity.ok(reservaService.calcularIngresosPorCantidadPersonas());
    }
}
