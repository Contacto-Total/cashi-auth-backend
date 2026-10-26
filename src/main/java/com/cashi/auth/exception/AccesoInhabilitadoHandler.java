package com.cashi.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Responde 403 con el mensaje para el asesor. Solo atiende esta excepcion:
 * el resto de errores del auth sigue como estaba.
 *
 * Tiene que ser 4xx y no 500: la pantalla de login muestra `message`, y el
 * front solo cierra la sesion cuando la renovacion del token falla con 4xx
 * (un 500 lo toma como servicio caido y deja la sesion abierta).
 */
@RestControllerAdvice
public class AccesoInhabilitadoHandler {

    @ExceptionHandler(AccesoInhabilitadoException.class)
    public ResponseEntity<Map<String, Object>> inhabilitado(AccesoInhabilitadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage(), "inhabilitado", true));
    }
}
