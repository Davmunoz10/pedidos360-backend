package com.pedidos360.ms_kafka_admin.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class TopicDtos {

    private TopicDtos() {
    }

    public record TopicRequest(
            @NotBlank(message = "name es obligatorio")
            @Pattern(regexp = Reglas.TOPIC_REGEX, message = "name " + Reglas.TOPIC_MENSAJE)
            String name,

            @NotNull(message = "partitions es obligatorio")
            @Min(value = 1, message = "partitions debe ser al menos 1")
            @Max(value = 50, message = "partitions no puede superar 50")
            Integer partitions,

            @NotNull(message = "replicationFactor es obligatorio")
            @Min(value = 1, message = "replicationFactor debe ser al menos 1")
            @Max(value = 3, message = "replicationFactor no puede superar 3")
            Integer replicationFactor,

            @Min(value = 1, message = "retentionMs debe ser mayor a 0")
            Long retentionMs,

            @Pattern(regexp = Reglas.CLEANUP_REGEX, message = "cleanupPolicy debe ser delete, compact o compact,delete")
            String cleanupPolicy,

            @Min(value = 1, message = "minInsyncReplicas debe ser al menos 1")
            Integer minInsyncReplicas) {
    }

    public record TopicConfigRequest(
            @Min(value = 1, message = "retentionMs debe ser mayor a 0")
            Long retentionMs,

            @Pattern(regexp = Reglas.CLEANUP_REGEX, message = "cleanupPolicy debe ser delete, compact o compact,delete")
            String cleanupPolicy,

            @Min(value = 1, message = "minInsyncReplicas debe ser al menos 1")
            Integer minInsyncReplicas) {
    }

    public record PartitionsRequest(
            @NotNull(message = "partitions es obligatorio")
            @Min(value = 1, message = "partitions debe ser al menos 1")
            @Max(value = 50, message = "partitions no puede superar 50")
            Integer partitions) {
    }

    public record TopicSummary(String name, int partitions, int replicationFactor) {
    }

    public record PartitionDetail(int partition, int leader, List<Integer> replicas, List<Integer> isr) {
    }

    public record TopicDetail(
            String name,
            int partitions,
            int replicationFactor,
            List<PartitionDetail> partitionDetails,
            Map<String, String> configs) {
    }
}