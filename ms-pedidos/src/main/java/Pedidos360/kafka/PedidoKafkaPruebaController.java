package Pedidos360.kafka;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/kafka")
public class PedidoKafkaPruebaController {

    private final PedidoEventoProducer productor;

    public PedidoKafkaPruebaController(PedidoEventoProducer productor) {
        this.productor = productor;
    }

    @PostMapping("/prueba")
    public ResponseEntity<Map<String, Object>> publicar(
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad,
            @RequestParam(name = "modo", defaultValue = "ok") String modo) {

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("cantidad", cantidad);
        respuesta.put("modo", modo);

        if (cantidad < 1 || cantidad > 100) {
            respuesta.put("error", "cantidad debe estar entre 1 y 100");
            return ResponseEntity.badRequest().body(respuesta);
        }
        if (!"ok".equals(modo) && !"temporal".equals(modo) && !"invalido".equals(modo)) {
            respuesta.put("error", "modo debe ser ok, temporal o invalido");
            return ResponseEntity.badRequest().body(respuesta);
        }

        for (int i = 0; i < cantidad; i++) {
            String pedidoId = "KP-" + System.currentTimeMillis() + "-" + i;
            if ("invalido".equals(modo)) {
                productor.publicarMensajeInvalido(pedidoId);
            } else {
                String email = "cliente" + ThreadLocalRandom.current().nextInt(1, 6) + "@correo.cl";
                BigDecimal total = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(5000, 80000))
                        .setScale(2, RoundingMode.HALF_UP);
                productor.publicar("PEDIDO_CREADO", pedidoId, email, total, modo);
            }
        }

        respuesta.put("resultado", "publicado");
        return ResponseEntity.ok(respuesta);
    }
}