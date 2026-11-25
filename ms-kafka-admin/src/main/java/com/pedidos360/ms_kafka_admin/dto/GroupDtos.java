package com.pedidos360.ms_kafka_admin.dto;

import java.util.List;

public final class GroupDtos {

    private GroupDtos() {
    }

    public record GroupSummary(String groupId, String state) {
    }

    public record MemberDetail(String consumerId, String clientId, String host, List<String> partitions) {
    }

    public record OffsetLag(String topic, int partition, long currentOffset, long endOffset, long lag) {
    }

    public record GroupDetail(
            String groupId,
            String state,
            int memberCount,
            List<MemberDetail> members,
            List<OffsetLag> offsets,
            long totalLag) {
    }
}