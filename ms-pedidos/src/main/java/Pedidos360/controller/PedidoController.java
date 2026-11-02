package Pedidos360.controller;

import Pedidos360.model.DetallePedido;
import Pedidos360.model.Pedido;
import Pedidos360.model.Producto;
import Pedidos360.repository.PedidoRepository;
import Pedidos360.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import Pedidos360.messaging.PedidoEventPublisher;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;

    @Autowired
    private PedidoEventPublisher eventPublisher;

    public PedidoController(PedidoRepository pedidoRepo, ProductoRepository productoRepo) {
        this.pedidoRepo = pedidoRepo;
        this.productoRepo = productoRepo;
    }

    // Con JWT activo toma el email del token; sin JWT usa un usuario de prueba
    private String emailDe(Jwt jwt) {
        if (jwt == null) return "dev@local";
        String email = jwt.getClaimAsString("preferred_username");
        return email != null ? email : jwt.getSubject();
    }

    @GetMapping
    public List<Pedido> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        return pedidoRepo.findByClienteEmail(emailDe(jwt));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido,
                                        @AuthenticationPrincipal Jwt jwt) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido no tiene detalles");
        }

        pedido.setId(null);
        pedido.setClienteEmail(emailDe(jwt));
        pedido.setEstado("CREADO");

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido d : pedido.getDetalles()) {
            if (d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
            }
            // El precio se toma de la BD, nunca del cliente
            Producto p = productoRepo.findById(d.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Producto no existe: " + d.getProductoId()));
            d.setId(null);
            d.setPrecioUnitario(p.getPrecio());
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(d.getCantidad())));
        }
        pedido.setTotal(total);

        // Guardamos el pedido en la base de datos
        Pedido pedidoGuardado = pedidoRepo.save(pedido);
        
        // Enviamos el evento del pedido creado hacia RabbitMQ usando el método correcto
        eventPublisher.publicarPedidoCreado(String.valueOf(pedidoGuardado.getId()));

        // Retornamos la respuesta HTTP con el pedido creado
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoGuardado);
    }
}