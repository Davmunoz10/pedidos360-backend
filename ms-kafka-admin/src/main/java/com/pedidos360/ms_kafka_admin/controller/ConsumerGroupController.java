package com.pedidos360.ms_kafka_admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.ms_kafka_admin.dto.GroupDtos.GroupDetail;
import com.pedidos360.ms_kafka_admin.dto.GroupDtos.GroupSummary;
import com.pedidos360.ms_kafka_admin.service.KafkaConsumerGroupService;

@RestController
@RequestMapping("/kafka/consumer-groups")
public class ConsumerGroupController {

    private final KafkaConsumerGroupService servicio;

    public ConsumerGroupController(KafkaConsumerGroupService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<GroupSummary> listar() {
        return servicio.listar();
    }

    @GetMapping("/{groupId}")
    public GroupDetail obtener(@PathVariable("groupId") String groupId) {
        return servicio.obtener(groupId);
    }
}