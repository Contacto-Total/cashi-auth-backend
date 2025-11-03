package com.cashi.auth.config;

import com.cashi.auth.service.ConfiguracionSesionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ConfiguracionSesionService configuracionSesionService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando configuraciones del sistema...");
        configuracionSesionService.inicializarConfiguracionesPorDefecto();
        log.info("Configuraciones del sistema inicializadas correctamente");
    }
}
