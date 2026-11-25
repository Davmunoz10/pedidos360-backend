package com.pedidos360.ms_kafka_admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.ms_kafka_admin.dto.ClusterDtos.ClusterInfo;
import com.pedidos360.ms_kafka_admin.service.KafkaClusterService;

@RestController
@RequestMapping("/kafka/cluster")
public class ClusterController {

    private final KafkaClusterService servicio;

    public ClusterController(KafkaClusterService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ClusterInfo describir() {
        return servicio.describir();
    }
}