package com.pedidos360.rabbitadmin.dto;

/**
 * Reglas de validacion compartidas por los DTO y por el servicio.
 */
public final class Reglas {

    private Reglas() {
    }

    public static final String NOMBRE_REGEX = "^(?!amq\\.)[A-Za-z0-9._-]{1,255}$";
    public static final String NOMBRE_MENSAJE =
            "debe tener entre 1 y 255 caracteres (letras, numeros, punto, guion o guion bajo), "
                    + "sin espacios y sin el prefijo reservado amq.";

    public static final String ROUTING_KEY_REGEX = "^[A-Za-z0-9._*#-]{0,255}$";
    public static final String ROUTING_KEY_MENSAJE =
            "solo puede tener letras, numeros, punto, guion, guion bajo, * y # (maximo 255 caracteres)";
}