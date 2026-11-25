package com.pedidos360.ms_kafka_admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pedidos360.ms_kafka_admin.dto.Reglas;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.PartitionDetail;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.PartitionsRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicConfigRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicDetail;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicRequest;
import com.pedidos360.ms_kafka_admin.dto.TopicDtos.TopicSummary;

@Service
public class KafkaTopicService {

    private static final Logger log = LoggerFactory.getLogger(KafkaTopicService.class);

    private static final List<String> CONFIGS_VISIBLES =
            List.of("retention.ms", "cleanup.policy", "min.insync.replicas", "max.message.bytes");

    private final Admin admin;
    private final KafkaSupport soporte;
    private final KafkaClusterService clusterService;

    public KafkaTopicService(Admin admin, KafkaSupport soporte, KafkaClusterService clusterService) {
        this.admin = admin;
        this.soporte = soporte;
        this.clusterService = clusterService;
    }

    public List<TopicSummary> listar() {
        Set<String> nombres = soporte.esperar(admin.listTopics().names());
        if (nombres.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, TopicDescription> descripciones =
                soporte.esperar(admin.describeTopics(nombres).allTopicNames());
        return descripciones.values().stream()
                .sorted(Comparator.comparing(TopicDescription::name))
                .map(descripcion -> new TopicSummary(
                        descripcion.name(), descripcion.partitions().size(), factorReplicacion(descripcion)))
                .toList();
    }

    public TopicDetail obtener(String nombre) {
        validarNombre(nombre);
        TopicDescription descripcion = describir(nombre);
        List<PartitionDetail> particiones = descripcion.partitions().stream()
                .sorted(Comparator.comparingInt(TopicPartitionInfo::partition))
                .map(particion -> new PartitionDetail(
                        particion.partition(),
                        particion.leader() == null ? -1 : particion.leader().id(),
                        ids(particion.replicas()),
                        ids(particion.isr())))
                .toList();
        return new TopicDetail(
                nombre,
                descripcion.partitions().size(),
                factorReplicacion(descripcion),
                particiones,
                configuraciones(nombre));
    }

    public TopicRequest crear(TopicRequest peticion) {
        int brokers = clusterService.cantidadBrokers();
        if (peticion.replicationFactor() > brokers) {
            throw new IllegalArgumentException("replicationFactor (" + peticion.replicationFactor()
                    + ") no puede superar la cantidad de brokers disponibles (" + brokers + ")");
        }
        if (peticion.minInsyncReplicas() != null && peticion.minInsyncReplicas() > peticion.replicationFactor()) {
            throw new IllegalArgumentException("minInsyncReplicas no puede superar replicationFactor");
        }

        Map<String, String> configs = new HashMap<>();
        if (peticion.retentionMs() != null) {
            configs.put("retention.ms", String.valueOf(peticion.retentionMs()));
        }
        if (peticion.cleanupPolicy() != null) {
            configs.put("cleanup.policy", peticion.cleanupPolicy());
        }
        if (peticion.minInsyncReplicas() != null) {
            configs.put("min.insync.replicas", String.valueOf(peticion.minInsyncReplicas()));
        }

        NewTopic nuevo = new NewTopic(
                peticion.name(), peticion.partitions(), peticion.replicationFactor().shortValue()).configs(configs);
        soporte.esperar(admin.createTopics(List.of(nuevo)).all());

        log.info("[KAFKA-ADMIN] Topico creado: {} (particiones={}, replicas={})",
                peticion.name(), peticion.partitions(), peticion.replicationFactor());
        return peticion;
    }

    public TopicDetail modificarConfiguracion(String nombre, TopicConfigRequest peticion) {
        validarNombre(nombre);
        List<AlterConfigOp> operaciones = new ArrayList<>();
        if (peticion.retentionMs() != null) {
            operaciones.add(fijar("retention.ms", String.valueOf(peticion.retentionMs())));
        }
        if (peticion.cleanupPolicy() != null) {
            operaciones.add(fijar("cleanup.policy", peticion.cleanupPolicy()));
        }
        if (peticion.minInsyncReplicas() != null) {
            operaciones.add(fijar("min.insync.replicas", String.valueOf(peticion.minInsyncReplicas())));
        }
        if (operaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos una configuracion: retentionMs, cleanupPolicy o minInsyncReplicas");
        }

        int replicas = factorReplicacion(describir(nombre));
        if (peticion.minInsyncReplicas() != null && peticion.minInsyncReplicas() > replicas) {
            throw new IllegalArgumentException(
                    "minInsyncReplicas no puede superar el factor de replicacion del topico (" + replicas + ")");
        }

        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Map<ConfigResource, Collection<AlterConfigOp>> cambios = new HashMap<>();
        cambios.put(recurso, operaciones);
        soporte.esperar(admin.incrementalAlterConfigs(cambios).all());

        log.info("[KAFKA-ADMIN] Configuracion actualizada en el topico {}", nombre);
        return obtener(nombre);
    }

    public TopicSummary aumentarParticiones(String nombre, PartitionsRequest peticion) {
        validarNombre(nombre);
        TopicDescription descripcion = describir(nombre);
        int actuales = descripcion.partitions().size();
        if (peticion.partitions() <= actuales) {
            throw new IllegalArgumentException("partitions debe ser mayor a las particiones actuales (" + actuales + ")");
        }
        soporte.esperar(admin.createPartitions(
                Map.of(nombre, NewPartitions.increaseTo(peticion.partitions()))).all());

        log.info("[KAFKA-ADMIN] Topico {}: particiones {} -> {}", nombre, actuales, peticion.partitions());
        return new TopicSummary(nombre, peticion.partitions(), factorReplicacion(descripcion));
    }

    public void eliminar(String nombre) {
        validarNombre(nombre);
        soporte.esperar(admin.deleteTopics(List.of(nombre)).all());
        log.info("[KAFKA-ADMIN] Topico eliminado: {}", nombre);
    }

    private TopicDescription describir(String nombre) {
        Map<String, TopicDescription> resultado =
                soporte.esperar(admin.describeTopics(List.of(nombre)).allTopicNames());
        return resultado.get(nombre);
    }

    private Map<String, String> configuraciones(String nombre) {
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Map<ConfigResource, Config> resultado = soporte.esperar(admin.describeConfigs(List.of(recurso)).all());
        Config config = resultado.get(recurso);
        Map<String, String> salida = new LinkedHashMap<>();
        for (String clave : CONFIGS_VISIBLES) {
            ConfigEntry entrada = config.get(clave);
            if (entrada != null) {
                salida.put(clave, entrada.value());
            }
        }
        return salida;
    }

    private AlterConfigOp fijar(String clave, String valor) {
        return new AlterConfigOp(new ConfigEntry(clave, valor), AlterConfigOp.OpType.SET);
    }

    private int factorReplicacion(TopicDescription descripcion) {
        return descripcion.partitions().isEmpty() ? 0 : descripcion.partitions().get(0).replicas().size();
    }

    private List<Integer> ids(List<Node> nodos) {
        return nodos.stream().map(Node::id).toList();
    }

    private void validarNombre(String nombre) {
        if (nombre == null || !nombre.matches(Reglas.TOPIC_REGEX)) {
            throw new IllegalArgumentException("name " + Reglas.TOPIC_MENSAJE);
        }
    }
}