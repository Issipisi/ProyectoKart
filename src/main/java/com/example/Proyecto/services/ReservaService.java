package com.example.Proyecto.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.FileSystemResource;

import jakarta.mail.internet.MimeMessage;

import com.example.Proyecto.entities.ClienteEntity;
import com.example.Proyecto.entities.ReservaEntity;
import com.example.Proyecto.entities.TarifaEntity;
import com.example.Proyecto.repositories.ClienteRepository;
import com.example.Proyecto.repositories.ReservaRepository;
import com.example.Proyecto.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Crear reserva
    public ReservaEntity crearReserva(
            Long clienteId,
            int cantidadVueltas,
            int cantidadPersonas,
            String fecha,
            boolean clienteFrecuente,
            boolean diaEspecial
    ) {
        ClienteEntity cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        TarifaEntity tarifa = calcularTarifaAutomatica(cantidadVueltas, diaEspecial);

        double montoBase = tarifa.getCosto();
        double descuento = calcularMejorDescuento(cantidadPersonas, clienteFrecuente, diaEspecial);
        double montoFinal = montoBase * (1 - descuento);
        String tipoDescuento = determinarTipoDescuento(descuento, cantidadPersonas, clienteFrecuente, diaEspecial);

        LocalDateTime fechaHora = LocalDateTime.parse(fecha);

        ReservaEntity reserva = ReservaEntity.builder()
                .clienteId(clienteId)
                .tarifaId(tarifa.getId())
                .cantidadPersonas(cantidadPersonas)
                .fecha(fechaHora)
                .montoBase(montoBase)
                .montoFinal(montoFinal)
                .tipoDescuentoAplicado(tipoDescuento)
                .build();
        return reservaRepository.save(reserva);
    }

    // Calcula las tarifas de forma automática en base a la reserva
    private TarifaEntity calcularTarifaAutomatica(int vueltas, boolean diaEspecial) {
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
                throw new IllegalArgumentException("Número de vueltas invalid");
        }
        Optional<TarifaEntity> tarifaExistente = tarifaRepository.findByVueltasAndCostoAndDuracion(
                vueltas, costo, minutos
        );

        if (tarifaExistente.isPresent()) {
            return tarifaExistente.get();
        } else {
            // Crear nueva tarifa
            TarifaEntity nuevaTarifa = TarifaEntity.builder()
                    .vueltas(vueltas)
                    .duracion(minutos)
                    .costo(costo)
                    .build();
            return tarifaRepository.save(nuevaTarifa);
        }

    }

    // Lógica para calcular el mejor descuento desde backend
    private double calcularMejorDescuento(int personas, boolean frecuente, boolean especial) {
        double dGrupo = (personas >= 11) ? 0.30 : (personas >= 6) ? 0.20 : (personas >= 3) ? 0.10 : 0.0;
        double dFrecuente = frecuente ? 0.30 : 0.0;
        double dEspecial = especial ? 0.20 : 0.0;
        return Math.max(dGrupo, Math.max(dFrecuente, dEspecial));
    }

    private String determinarTipoDescuento(double descuento, int personas, boolean frecuente, boolean especial) {
        if (frecuente && descuento == 0.30) return "FRECUENTE";
        if (especial && descuento == 0.20) return "ESPECIAL";
        if (personas >= 11 && descuento == 0.30) return "GRUPO 11+";
        if (personas >= 6 && descuento == 0.20) return "GRUPO 6+";
        if (personas >= 3 && descuento == 0.10) return "GRUPO 3+";
        return "NINGUNO";
    }

    // Listar todas las reservas con nombres de cliente (modo texto)
    public List<ReservaEntity> listarReservas() {
        return reservaRepository.findAll();
    }

    // Obtener reserva por ID con detalles formateados
    public String obtenerReservaDetalle(Long id) {
        ReservaEntity reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reserva no encontrada"));

        ClienteEntity cliente = clienteRepository.findById(reserva.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente no encontrado"));

        TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tarifa no encontrada"));

        return "Reserva # " + reserva.getId() +
                "\nCliente: " + cliente.getNombre() +
                "\nCantidad de Personas: " + reserva.getCantidadPersonas() +
                "\nFecha: " + reserva.getFecha() +
                "\nTarifa Base: $" + reserva.getMontoBase() +
                "\nVueltas: " + tarifa.getVueltas() +
                "\nDescuento aplicado: " + reserva.getTipoDescuentoAplicado() +
                "\nMonto Final: $" + reserva.getMontoFinal();
    }

    public String generarComprobanteReserva(Long reservaId) {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ClienteEntity cliente = clienteRepository.findById(reserva.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));

        double iva = reserva.getMontoFinal() * 0.19;  // IVA 19%
        double totalConIVA = reserva.getMontoFinal() + iva;

        // Comprobante en forma de Texto
        String comprobante = "-------------------------------------------------\n" +
                "COMPROBANTE DE RESERVA #" + reserva.getId() + "\n" +
                "Fecha y Hora de Reserva: " + reserva.getFecha() + "\n" +
                "Cliente: " + cliente.getNombre() + "\n" +
                "Cantidad Personas: " + reserva.getCantidadPersonas() + "\n" +
                "Número de Vueltas: " + tarifa.getVueltas() + " vueltas\n" +
                "Duración Estimada: " + tarifa.getDuracion() + " minutos\n" +
                "-------------------------------------------------\n" +
                "Tarifa Base: $" + reserva.getMontoBase() + "\n" +
                "Descuento aplicado: " + reserva.getTipoDescuentoAplicado() + "\n" +
                "Monto Final sin IVA: $" + reserva.getMontoFinal() + "\n" +
                "IVA (19%): $" + String.format("%.2f", iva) + "\n" +
                "TOTAL a Pagar: $" + String.format("%.2f", totalConIVA) + "\n" +
                "-------------------------------------------------";

        return comprobante;
    }

    //Generar el archivo txt para el comprobante
    public File generarComprobanteArchivo(Long reservaId) throws IOException {
        String comprobanteTexto = generarComprobanteReserva(reservaId);

        // Crear archivo temporal
        File file = File.createTempFile("comprobante_" + reservaId, ".txt");
        FileWriter writer = new FileWriter(file);
        writer.write(comprobanteTexto);
        writer.close();

        return file;
    }

    //Envía el comprobante por correo
    public void enviarComprobantePorCorreo(Long reservaId) throws Exception {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ClienteEntity cliente = clienteRepository.findById(reserva.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        File comprobante = generarComprobanteArchivo(reservaId);

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(cliente.getEmail());
        helper.setSubject("Comprobante de Reserva #" + reserva.getId());
        helper.setText("Estimado/a " + cliente.getNombre() + ",\n\nAdjunto encontrarás el comprobante de tu reserva.");

        // Agregar comprobante como adjunto
        FileSystemResource file = new FileSystemResource(comprobante);
        helper.addAttachment("comprobante_reserva_" + reservaId + ".txt", file);

        mailSender.send(mensaje);
    }

    // Reporte de ingresos agrupado por cantidad de vueltas
    public Map<Integer, Double> calcularIngresosPorVueltas() {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<Integer, Double> ingresosPorVueltas = new HashMap<>();

        for (ReservaEntity reserva : reservas) {
            TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));

            int vueltas = tarifa.getVueltas();
            ingresosPorVueltas.put(vueltas, ingresosPorVueltas.getOrDefault(vueltas, 0.0) +
                    reserva.getMontoFinal());
        }

        return ingresosPorVueltas;
    }

    // Reporte de ingresos agrupado por número de personas
    public Map<Integer, Double> calcularIngresosPorCantidadPersonas() {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<Integer, Double> ingresosPorPersonas = new HashMap<>();

        for (ReservaEntity reserva : reservas) {
            int personas = reserva.getCantidadPersonas();
            ingresosPorPersonas.put(personas, ingresosPorPersonas.getOrDefault(personas, 0.0) +
                    reserva.getMontoFinal());
        }
        return ingresosPorPersonas;
    }

}