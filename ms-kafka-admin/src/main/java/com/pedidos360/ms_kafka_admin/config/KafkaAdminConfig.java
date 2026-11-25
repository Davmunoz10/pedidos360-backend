package com.pedidos360.ms_kafka_admin.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaAdminConfig {

    @Bean(destroyMethod = "close")
    public Admin kafkaAdminClient(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.admin.request-timeout-ms}") int requestTimeoutMs) {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propiedades.put(AdminClientConfig.CLIENT_ID_CONFIG, "ms-kafka-admin");
        propiedades.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        propiedades.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, requestTimeoutMs * 2);
        return Admin.create(propiedades);
    }
}