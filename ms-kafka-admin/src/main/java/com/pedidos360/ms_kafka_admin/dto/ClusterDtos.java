package com.pedidos360.ms_kafka_admin.dto;

import java.util.List;

public final class ClusterDtos {

    private ClusterDtos() {
    }

    public record BrokerInfo(int id, String host, int port) {
    }

    public record ClusterInfo(String clusterId, int controllerId, List<BrokerInfo> brokers) {
    }
}