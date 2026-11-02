# ms-rabbit-admin

Microservicio administrador de RabbitMQ (puerto 8084). Todos los endpoints requieren un JWT valido de Azure AD.

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| POST | /admin/queues | Crea una cola. Body: name, durable, deadLetterExchange, deadLetterRoutingKey, messageTtlMs | 201, 400, 404, 409 |
| GET | /admin/queues/{name} | Consulta una cola (mensajes y consumidores) | 200, 404 |
| DELETE | /admin/queues/{name} | Elimina una cola | 204, 404 |
| POST | /admin/exchanges | Crea un exchange. Body: name, type (direct, topic, fanout, headers), durable | 201, 400, 409 |
| DELETE | /admin/exchanges/{name} | Elimina un exchange | 204, 404 |
| POST | /admin/bindings | Une un exchange con una cola. Body: exchange, queue, routingKey | 201, 400, 404 |
| DELETE | /admin/bindings?exchange=&queue=&routingKey= | Elimina un binding | 204, 400, 404 |

Errores: siempre con el formato `{status, error, mensaje, detalles, timestamp}`.

Reglas de nombres: entre 1 y 255 caracteres (letras, numeros, punto, guion o guion bajo), sin espacios y sin el prefijo `amq.`.