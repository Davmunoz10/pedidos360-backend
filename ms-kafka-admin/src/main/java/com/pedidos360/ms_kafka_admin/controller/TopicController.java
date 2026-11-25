package com.pedidos360.ms_kafka_admin.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.ms_kafka_admin.dto.TopicDtos.PartitionsRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicConfigRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicDetail;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicSummary;
import com.pedidos360.ms_kafka_admin.service.KafkaTopicService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/kafka/topics")
public class TopicController {

    private final KafkaTopicService servicio;

    public TopicController(KafkaTopicService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<TopicSummary> listar() {
        return servicio.listar();
    }

    @GetMapping("/{name}")
    public TopicDetail obtener(@PathVariable("name") String name) {
        return servicio.obtener(name);
    }

    @PostMapping
    public ResponseEntity<TopicRequest> crear(@Valid @RequestBody TopicRequest peticion) {
        TopicRequest creado = servicio.crear(peticion);
        return ResponseEntity.created(URI.create("/kafka/topics/" + creado.name())).body(creado);
    }

    @PutMapping("/{name}/config")
    public TopicDetail modificarConfiguracion(
            @PathVariable("name") String name, @Valid @RequestBody TopicConfigRequest peticion) {
        return servicio.modificarConfiguracion(name, peticion);
    }

    @PutMapping("/{name}/partitions")
    public TopicSummary aumentarParticiones(
            @PathVariable("name") String name, @Valid @RequestBody PartitionsRequest peticion) {
        return servicio.aumentarParticiones(name, peticion);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> eliminar(@PathVariable("name") String name) {
        servicio.eliminar(name);
        return ResponseEntity.noContent().build();
    }
}    

