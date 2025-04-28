package com.example.Proyecto.services;

import com.example.Proyecto.entities.ClienteEntity;
import com.example.Proyecto.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Crear un nuevo cliente
    public ClienteEntity registrarCliente(ClienteEntity cliente) {
        return clienteRepository.save(cliente);
    }

    //Listar todos los clientes
    public List<ClienteEntity> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // Buscar cliente por ID
    public Optional<ClienteEntity> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }


    //Se podría utilizar una función parecida para que todas las reservas de los clientes a inicios de mes sean 0
    /* Eliminar cliente para uso del administrador. Aunque se utilizaría en un caso futuro de limpieza de registros
     public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }*/

}