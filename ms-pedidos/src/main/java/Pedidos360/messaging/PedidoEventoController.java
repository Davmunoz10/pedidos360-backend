package Pedidos360.messaging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/eventos")
public class PedidoEventoController {

    private final PedidoEventPublisher publisher;

    public PedidoEventoController(PedidoEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/prueba")
    public ResponseEntity<Map<String, String>> publicarPrueba(
            @RequestParam(name = "routingKey", defaultValue = "pedido.creado") String routingKey,
            @RequestParam(name = "modo", defaultValue = "ok") String modo) {

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("routingKey", routingKey);
        respuesta.put("modo", modo);

        if ("invalido".equals(modo)) {
            publisher.publicarMensajeInvalido(routingKey);
        } else if ("ok".equals(modo) || "temporal".equals(modo)) {
            publisher.publicar(routingKey,
                    new PedidoEvent("PRUEBA-" + System.currentTimeMillis(), "CREADO", modo));
        } else {
            respuesta.put("error", "modo debe ser ok, temporal o invalido");
            return ResponseEntity.badRequest().body(respuesta);
        }

        respuesta.put("resultado", "publicado");
        return ResponseEntity.ok(respuesta);
    }
}