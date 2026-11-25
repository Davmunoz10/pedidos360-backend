package com.pedidos360.ms_kafka_admin.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;

import com.pedidos360.ms_kafka_admin.dto.ClusterDtos.BrokerInfo;
import com.pedidos360.ms_kafka_admin.dto.ClusterDtos.ClusterInfo;

@Service
public class KafkaClusterService {

    private final Admin admin;
    private final KafkaSupport soporte;

    public KafkaClusterService(Admin admin, KafkaSupport soporte) {
        this.admin = admin;
        this.soporte = soporte;
    }

    public ClusterInfo describir() {
        DescribeClusterResult resultado = admin.describeCluster();
        String clusterId = soporte.esperar(resultado.clusterId());
        Node controlador = soporte.esperar(resultado.controller());
        Collection<Node> nodos = soporte.esperar(resultado.nodes());
        List<BrokerInfo> brokers = nodos.stream()
                .sorted(Comparator.comparingInt(Node::id))
                .map(nodo -> new BrokerInfo(nodo.id(), nodo.host(), nodo.port()))
                .toList();
        int idControlador = (controlador == null) ? -1 : controlador.id();
        return new ClusterInfo(clusterId, idControlador, brokers);
    }

    public int cantidadBrokers() {
        return soporte.esperar(admin.describeCluster().nodes()).size();
    }
}