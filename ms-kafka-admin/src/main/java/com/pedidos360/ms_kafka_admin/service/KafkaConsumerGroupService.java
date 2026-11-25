package com.pedidos360.ms_kafka_admin.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

import com.pedidos360.ms_kafka_admin.dto.GroupDtos.GroupDetail;
import com.pedidos360.ms_kafka_admin.dto.GroupDtos.GroupSummary;
import com.pedidos360.ms_kafka_admin.dto.GroupDtos.MemberDetail;
import com.pedidos360.ms_kafka_admin.dto.GroupDtos.OffsetLag;
import com.pedidos360.ms_kafka_admin.dto.Reglas;
import com.pedidos360.ms_kafka_admin.exception.RecursoNoEncontradoException;

@Service
public class KafkaConsumerGroupService {

    private final Admin admin;
    private final KafkaSupport soporte;

    public KafkaConsumerGroupService(Admin admin, KafkaSupport soporte) {
        this.admin = admin;
        this.soporte = soporte;
    }

    public List<GroupSummary> listar() {
        Collection<ConsumerGroupListing> grupos = soporte.esperar(admin.listConsumerGroups().all());
        return grupos.stream()
                .sorted(Comparator.comparing(ConsumerGroupListing::groupId))
                .map(grupo -> new GroupSummary(
                        grupo.groupId(), grupo.state().map(Object::toString).orElse("DESCONOCIDO")))
                .toList();
    }

    public GroupDetail obtener(String groupId) {
        validarGrupo(groupId);

        Map<String, ConsumerGroupDescription> descripciones =
                soporte.esperar(admin.describeConsumerGroups(List.of(groupId)).all());
        ConsumerGroupDescription descripcion = descripciones.get(groupId);
        if (descripcion == null || "dead".equalsIgnoreCase(String.valueOf(descripcion.state()))) {
            throw new RecursoNoEncontradoException("El consumer group '" + groupId + "' no existe");
        }

        List<MemberDetail> miembros = descripcion.members().stream()
                .map(miembro -> new MemberDetail(
                        miembro.consumerId(),
                        miembro.clientId(),
                        miembro.host(),
                        miembro.assignment().topicPartitions().stream()
                                .map(tp -> tp.topic() + "-" + tp.partition())
                                .sorted()
                                .toList()))
                .toList();

        Map<TopicPartition, OffsetAndMetadata> confirmados =
                soporte.esperar(admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());

        Map<TopicPartition, OffsetSpec> consultas = new HashMap<>();
        confirmados.keySet().forEach(tp -> consultas.put(tp, OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResultInfo> finales = consultas.isEmpty()
                ? Map.of()
                : soporte.esperar(admin.listOffsets(consultas).all());

        List<OffsetLag> offsets = confirmados.entrySet().stream()
                .filter(entrada -> entrada.getValue() != null)
                .map(entrada -> {
                    TopicPartition tp = entrada.getKey();
                    long actual = entrada.getValue().offset();
                    long fin = finales.get(tp).offset();
                    return new OffsetLag(tp.topic(), tp.partition(), actual, fin, Math.max(0, fin - actual));
                })
                .sorted(Comparator.comparing(OffsetLag::topic).thenComparingInt(OffsetLag::partition))
                .toList();

        long lagTotal = offsets.stream().mapToLong(OffsetLag::lag).sum();
        return new GroupDetail(groupId, String.valueOf(descripcion.state()), miembros.size(), miembros, offsets, lagTotal);
    }

    private void validarGrupo(String groupId) {
        if (groupId == null || !groupId.matches(Reglas.GRUPO_REGEX)) {
            throw new IllegalArgumentException("groupId " + Reglas.GRUPO_MENSAJE);
        }
    }
}