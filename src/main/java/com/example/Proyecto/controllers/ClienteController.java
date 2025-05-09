package com.example.Proyecto.controllers;

import com.example.Proyecto.entities.ClienteEntity;
import com.example.Proyecto.services.ClienteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5174",
        "http://40.82.176.155"
})
@RequestMapping("/api/clientes") // Ruta base para todas las operaciones de Cliente
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Crear nuevo cliente
    @PostMapping
    public ResponseEntity<ClienteEntity> registrarCliente(@RequestBody ClienteEntity cliente) {
        ClienteEntity clienteCreado = clienteService.registrarCliente(cliente);
        return new ResponseEntity<>(clienteCreado, HttpStatus.CREATED);
    }

    // Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteEntity> obtenerPorId(@PathVariable Long id) {
        Optional<ClienteEntity> cliente = clienteService.buscarPorId(id);
        return cliente.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar todos los clientes
    @GetMapping
    public ResponseEntity<List<ClienteEntity>> listarClientes() {
        List<ClienteEntity> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }



    /*Eliminar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }*/
}
