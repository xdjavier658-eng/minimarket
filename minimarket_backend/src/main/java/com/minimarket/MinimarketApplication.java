package com.minimarket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinimarketApplication {

    private static final Logger logger = LoggerFactory.getLogger(MinimarketApplication.class);

    public static void main(String[] args) {
        logger.info("Iniciando Minimarket Backend...");
        SpringApplication.run(MinimarketApplication.class, args);
        logger.info("\n=========================================");
        logger.info("MINIMARKET BACKEND INICIADO CORRECTAMENTE");
        logger.info("=========================================");
        logger.info("🌐 URL: http://localhost:8080/api");
        logger.info("📊 Estado: http://localhost:8080/api/public/status");
        logger.info("🔑 Login: POST http://localhost:8080/api/auth/signin");
        logger.info("   Usuario: admin");
        logger.info("   Contraseña: admin123");
        logger.info("=========================================\n");
    }
}