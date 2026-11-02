package com.pedidos360.rabbitadmin.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.rabbitadmin.dto.BindingRequest;
import com.pedidos360.rabbitadmin.dto.ExchangeRequest;
import com.pedidos360.rabbitadmin.dto.QueueInfoResponse;
import com.pedidos360.rabbitadmin.dto.QueueRequest;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;

/**
 * API REST para administrar RabbitMQ.
 *
 * POST   /admin/queues                                          crea una cola         (201, 400, 404, 409)
 * GET    /admin/queues/{name}                                   consulta una cola     (200, 404)
 * DELETE /admin/queues/{name}                                   elimina una cola      (204, 404)
 * POST   /admin/exchanges                                       crea un exchange      (201, 400, 409)
 * DELETE /admin/exchanges/{name}                                elimina un exchange   (204, 404)
 * POST   /admin/bindings                                        crea un binding       (201, 400, 404)
 * DELETE /admin/bindings?exchange=&queue=&routingKey=           elimina un binding    (204, 400, 404)
 */
@RestController
@RequestMapping("/admin")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ---------- Colas ----------

    @PostMapping("/queues")
    public ResponseEntity<QueueRequest> crearCola(@Valid @RequestBody QueueRequest request) {
        QueueRequest creada = service.crearCola(request);
        return ResponseEntity.created(URI.create("/admin/queues/" + creada.name())).body(creada);
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse obtenerCola(@PathVariable("name") String name) {
        return service.obtenerCola(name);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> eliminarCola(@PathVariable("name") String name) {
        service.eliminarCola(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Exchanges ----------

    @PostMapping("/exchanges")
    public ResponseEntity<ExchangeRequest> crearExchange(@Valid @RequestBody ExchangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearExchange(request));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable("name") String name) {
        service.eliminarExchange(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Bindings ----------

    @PostMapping("/bindings")
    public ResponseEntity<BindingRequest> crearBinding(@Valid @RequestBody BindingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBinding(request));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @RequestParam("exchange") String exchange,
            @RequestParam("queue") String queue,
            @RequestParam(name = "routingKey", defaultValue = "") String routingKey) {
        service.eliminarBinding(exchange, queue, routingKey);
        return ResponseEntity.noContent().build();
    }
}
