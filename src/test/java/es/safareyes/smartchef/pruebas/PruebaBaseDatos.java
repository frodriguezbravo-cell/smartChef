package es.safareyes.smartchef.pruebas;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/** Base de los tests de repositorio que necesitan preparar datos con SQL nativo. */
public abstract class PruebaBaseDatos {

    @Autowired
    protected EntityManager em;

    protected BD bd() {
        return new BD(em);
    }
}
