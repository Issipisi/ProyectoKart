package com.example.Proyecto.services;

import jakarta.transaction.Transactional;
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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private TarifaService tarifaService;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    // Crear reserva
    public ReservaEntity crearReserva(
            Long clienteId,
            int cantidadVueltas,
            int cantidadPersonas,
            String fecha,
            boolean diaEspecial,
            boolean cumpleanos
    ) {
        ClienteEntity cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Aumentamos su contador de reservas
        cliente.setTotal_reservas(cliente.getTotal_reservas() + 1);
        clienteRepository.save(cliente); // Actualizamos cliente

        // Crear o buscar tarifa
        TarifaEntity tarifa = tarifaService.buscarOCrearTarifa(cantidadVueltas, diaEspecial);

        double montoBase = tarifa.getCosto();
        double descuento = calcularMejorDescuento(cantidadPersonas, cliente, diaEspecial, cumpleanos);
        String tipoDescuento = determinarTipoDescuento(descuento, cantidadPersonas, cliente, diaEspecial, cumpleanos);
        double subtotal = montoBase * (1 - descuento);
        double iva = subtotal * 0.19;
        double montoFinal = subtotal + iva;
        montoFinal = Math.round(montoFinal * 100.0) / 100.0; //Para que tenga pocos decimales

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

    // Calcular descuento de cumpleaños
    private double calcularDescuentoCumpleanos(boolean cumpleanos) {
        return cumpleanos ? 0.50 : 0.0;  // 50% de descuento
    }

    // Calcular descuento por número de personas
    private double calcularDescuentoPorGrupo(int personas) {
        if (personas >= 11) return 0.30;
        if (personas >= 6) return 0.20;
        if (personas >= 3) return 0.10;
        return 0.0;
    }

    // Calcular descuento por frecuencia del cliente
    private double calcularDescuentoPorClienteFrecuente(int totalReservasAlMes) {
        if (totalReservasAlMes >= 7) return 0.30;
        if (totalReservasAlMes >= 5) return 0.20;
        if (totalReservasAlMes >= 2) return 0.10;
        return 0.0;
    }

    // Determinar el mejor descuento automático
    private double calcularMejorDescuento(int personas, ClienteEntity cliente, boolean diaEspecial, boolean cumpleanos) {
        double descuentoGrupo = calcularDescuentoPorGrupo(personas);
        double descuentoFidelidad = calcularDescuentoPorClienteFrecuente(cliente.getTotal_reservas());
        double descuentoEspecial = diaEspecial ? 0.20 : 0.0;
        double descuentoCumpleanos = calcularDescuentoCumpleanos(cumpleanos);

        // retorna el descuento más alto de los cuatro
        return Math.max(
                descuentoCumpleanos,
                Math.max(descuentoGrupo, Math.max(descuentoFidelidad, descuentoEspecial))
        );
    }


    // Determinar tipo de descuento aplicado (comprobante)
    private String determinarTipoDescuento(double descuento, int personas, ClienteEntity cliente,
            boolean diaEspecial, boolean cumpleanos) {
        double descuentoGrupo = calcularDescuentoPorGrupo(personas);
        double descuentoFidelidad = calcularDescuentoPorClienteFrecuente(cliente.getTotal_reservas());
        double descuentoEspecial = diaEspecial ? 0.20 : 0.0;
        double descuentoCumpleanos = calcularDescuentoCumpleanos(cumpleanos);

        if (descuento == descuentoCumpleanos && descuento != 0.0) return "CUMPLEAÑOS";
        if (descuento == descuentoFidelidad && descuento != 0.0) return "FRECUENTE";
        if (descuento == descuentoEspecial && descuento != 0.0) return "DÍA ESPECIAL";
        if (descuento == descuentoGrupo && descuento != 0.0) {
            if (personas >= 11) return "GRUPO 11-15";
            if (personas >= 6) return "GRUPO 6-10";
            if (personas >= 3) return "GRUPO 3-5";
        }
        return "SIN DESCUENTO";
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

    public File generarComprobantePDF(Long reservaId) throws IOException, DocumentException {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ClienteEntity cliente = clienteRepository.findById(reserva.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));

        // Crear archivo temporal
        File file = File.createTempFile("comprobante_reserva_" + reservaId, ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Estilos
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font subTituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Título Principal
        Paragraph titulo = new Paragraph("🏎️ COMPROBANTE DE RESERVA #" + reservaId, tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Información del cliente
        document.add(new Paragraph(" Cliente: " + cliente.getNombre(), normalFont));
        document.add(new Paragraph(" Correo: " + cliente.getEmail(), normalFont));
        document.add(new Paragraph(" Fecha de la Reserva: " + (reserva.getFecha() != null ?
                reserva.getFecha().toString().replace("T", " ") : "No Asignada")
                , normalFont));
        document.add(new Paragraph(" ", normalFont));

        // Información de la Reserva
        Paragraph sectionTitle = new Paragraph("Detalles de la Reserva:", subTituloFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        document.add(new Paragraph(" Vueltas contratadas: " + tarifa.getVueltas(), normalFont));
        document.add(new Paragraph(" Duración Estimada: " + tarifa.getDuracion() + " minutos", normalFont));
        document.add(new Paragraph(" Cantidad de Personas: " + reserva.getCantidadPersonas(), normalFont));
        document.add(new Paragraph(" Monto Base: $" + reserva.getMontoBase(), normalFont));
        document.add(new Paragraph(" Descuento aplicado: " + reserva.getTipoDescuentoAplicado(), normalFont));
        document.add(new Paragraph(" Subtotal: $" + (reserva.getMontoFinal() / 1.19), normalFont));
        //Para que aparezca de forma correcta el IVA
        double subtotal = reserva.getMontoFinal() / 1.19;
        double iva = reserva.getMontoFinal() - subtotal;
        document.add(new Paragraph(" IVA (19%): $" + iva, normalFont));
        document.add(new Paragraph(" Total a Pagar: $" + reserva.getMontoFinal(), normalFont));

        document.add(new Paragraph(" ", normalFont));
        document.add(new Paragraph("-------------------------------------------"));

        // Mensaje agradecimiento
        Paragraph gracias = new Paragraph("¡Gracias por preferir nuestro Servicio de Karting! 🏁", normalFont);
        gracias.setAlignment(Element.ALIGN_CENTER);
        gracias.setSpacingBefore(20);
        document.add(gracias);

        document.close();

        return file;
    }


    public void enviarComprobantePorCorreo(Long reservaId) throws Exception {
        ReservaEntity reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ClienteEntity cliente = clienteRepository.findById(reserva.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Generar comprobante en PDF
        File comprobantePDF = generarComprobantePDF(reservaId);

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(cliente.getEmail());
        helper.setSubject(" Comprobante de Reserva #" + reservaId + " en KARTS");
        helper.setText("¡Hola " + cliente.getNombre() + "!\n\nAdjuntamos tu comprobante de reserva"
                + "\n Gracias por preferirnos. ");

        FileSystemResource file = new FileSystemResource(comprobantePDF);
        helper.addAttachment("comprobante_reserva_" + reservaId + ".pdf", file);

        mailSender.send(mensaje);
    }

    /* Reporte de ingresos agrupado por personas
    public Map<String, Map<String, Double>> calcularIngresosPorPersonasPorMes() {
        return calcularIngresosAgrupados(reserva -> {
            int personas = reserva.getCantidadPersonas();
            if (personas >= 1 && personas <= 2) return "1-2 personas";
            else if (personas >= 3 && personas <= 5) return "3-5 personas";
            else if (personas >= 6 && personas <= 10) return "6-10 personas";
            else if (personas >= 11 && personas <= 15) return "11-15 personas";
            else return "16 o más personas";
        });
    }

    // Reporte de ingresos agrupado por vueltas
    public Map<String, Map<String, Double>> calcularIngresosPorVueltasPorMes() {
        return calcularIngresosAgrupados(reserva -> {
            TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                    .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));
            return tarifa.getVueltas() + " vueltas";
        });
    }

    // Lo aplicamos para calcular los datos relacionados al mes en los reportes por personas o vueltas
    private Map<String, Map<String, Double>> calcularIngresosAgrupados(Function<ReservaEntity, String> agrupador) {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<String, Map<String, Double>> resultado = new HashMap<>();

        for (ReservaEntity reserva : reservas) {
            LocalDateTime fechaReserva = reserva.getFecha();
            if (fechaReserva == null) continue;

            String mes = fechaReserva.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();
            String claveAgrupacion = agrupador.apply(reserva);

            resultado.putIfAbsent(claveAgrupacion, new HashMap<>());
            Map<String, Double> ingresosPorMes = resultado.get(claveAgrupacion);

            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0.0) + reserva.getMontoFinal());
            ingresosPorMes.put("TOTAL", ingresosPorMes.getOrDefault("TOTAL", 0.0) + reserva.getMontoFinal());
        }

        return resultado;
    }*/

    // Reporte de ingresos por vueltas por mes
    public Map<String, Map<String, Double>> calcularIngresosPorVueltasPorMes(int mesInicio, int mesFin, int anio) {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<String, Map<String, Double>> resultado = new HashMap<>();

        for (ReservaEntity reserva : reservas) {
            LocalDateTime fechaReserva = reserva.getFecha();
            if (fechaReserva == null) continue;

            int mes = fechaReserva.getMonthValue();
            int anyo = fechaReserva.getYear();
            if (anyo != anio || mes < mesInicio || mes > mesFin) continue;

            String mesTexto = fechaReserva.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();

            TarifaEntity tarifa = tarifaRepository.findById(reserva.getTarifaId())
                    .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));
            String grupo = tarifa.getVueltas() + " vueltas";

            resultado.putIfAbsent(grupo, new HashMap<>());
            Map<String, Double> ingresosPorMes = resultado.get(grupo);

            ingresosPorMes.put(mesTexto, ingresosPorMes.getOrDefault(mesTexto, 0.0) + reserva.getMontoFinal());
            ingresosPorMes.put("TOTAL", ingresosPorMes.getOrDefault("TOTAL", 0.0) + reserva.getMontoFinal());
        }
        return resultado;
    }


    // Reporte de ingresos por personas por mes
    public Map<String, Map<String, Double>> calcularIngresosPorPersonasPorMes(int mesInicio, int mesFin, int anio) {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<String, Map<String, Double>> resultado = new HashMap<>();

        for (ReservaEntity reserva : reservas) {
            LocalDateTime fechaReserva = reserva.getFecha();
            if (fechaReserva == null) continue;

            int mes = fechaReserva.getMonthValue();
            int anyo = fechaReserva.getYear();
            if (anyo != anio || mes < mesInicio || mes > mesFin) continue;

            String mesTexto = fechaReserva.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();

            int personas = reserva.getCantidadPersonas();
            String grupo = "";
            if (personas >= 1 && personas <= 2) grupo = "1-2 personas";
            else if (personas >= 3 && personas <= 5) grupo = "3-5 personas";
            else if (personas >= 6 && personas <= 10) grupo = "6-10 personas";
            else if (personas >= 11 && personas <= 15) grupo = "11-15 personas";
            else grupo = "16 o más personas";

            resultado.putIfAbsent(grupo, new HashMap<>());
            Map<String, Double> ingresosPorMes = resultado.get(grupo);

            ingresosPorMes.put(mesTexto, ingresosPorMes.getOrDefault(mesTexto, 0.0) + reserva.getMontoFinal());
            ingresosPorMes.put("TOTAL", ingresosPorMes.getOrDefault("TOTAL", 0.0) + reserva.getMontoFinal());
        }
        return resultado;
    }

    //Para realizar el Rack semanal
    public Map<LocalDate, List<ReservaEntity>> getReservasPorSemana(LocalDate fechaInicio) {
        LocalDate fechaFin = fechaInicio.plusDays(6); // Semana de 7 días
        List<ReservaEntity> reservas = reservaRepository.findByFechaBetween(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59)
        );

        // Agrupar por fecha
        return reservas.stream()
                .collect(Collectors.groupingBy(
                        reserva -> reserva.getFecha().toLocalDate()
                ));
    }

    // Eliminar Reserva
    public void eliminarReserva(Long id) {
        reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        reservaRepository.deleteById(id);
    }

}