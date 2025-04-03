package com.example.Proyecto.services;


import com.example.Proyecto.entities.ClienteEntity;
import com.example.Proyecto.entities.KartEntity;
import com.example.Proyecto.repositories.KartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KartService {
    @Autowired
    private KartRepository kartRepository;

    // Crear o guardar un nuevo kart
    public KartEntity registrarKart(KartEntity kart) {
        return kartRepository.save(kart);
    }

    //Listar todos los Kart
    public List<KartEntity> obtenerTodosLosKart() {
        return kartRepository.findAll();
    }

    /*Buscar kart por codigo
    public Optional<KartEntity> buscarPorCodigo(String codigo) {
        return kartRepository.findByCodigo(codigo);
    }*/


}
