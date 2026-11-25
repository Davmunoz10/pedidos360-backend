package com.pedidos360.ms_kafka_admin.dto;

public final class Reglas {

    private Reglas() {
    }

    public static final String TOPIC_REGEX = "^(?!__)(?!\\.{1,2}$)[A-Za-z0-9._-]{1,249}$";
    public static final String TOPIC_MENSAJE =
            "debe tener entre 1 y 249 caracteres (letras, numeros, punto, guion o guion bajo), sin espacios y sin empezar con __";

    public static final String GRUPO_REGEX = "^[A-Za-z0-9._-]{1,255}$";
    public static final String GRUPO_MENSAJE =
            "debe tener entre 1 y 255 caracteres (letras, numeros, punto, guion o guion bajo) y sin espacios";

    public static final String CLEANUP_REGEX = "^(delete|compact|compact,delete)$";
}