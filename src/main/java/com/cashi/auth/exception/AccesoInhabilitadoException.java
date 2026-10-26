package com.cashi.auth.exception;

/**
 * El usuario existe y su contraseña es correcta, pero RR.HH. lo inhabilitó
 * (descanso, licencia o vacaciones). El mensaje va tal cual a la pantalla de
 * login: lo lee el asesor, no sistemas.
 */
public class AccesoInhabilitadoException extends RuntimeException {

    public AccesoInhabilitadoException(String mensaje) {
        super(mensaje);
    }
}
