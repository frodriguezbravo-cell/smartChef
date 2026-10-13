package es.safareyes.smartchef;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Arranca la aplicación completa. Con ddl-auto=validate, Hibernate comprueba que cada
 * entidad coincide con las tablas y columnas del script SQL. Si algo no coincide, falla aquí.
 * Requisitos: base de datos smartchefdb creada, script_smartchefdb.sql ejecutado y .env rellenado.
 */
@SpringBootTest
class SmartChefApplicationTests {

    @Test
    void contextLoads() {
    }
}
