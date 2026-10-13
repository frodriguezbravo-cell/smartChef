package es.safareyes.smartchef.pruebas;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;

/**
 * Prepara datos de prueba directamente en la base de datos con SQL nativo.
 * Se usa desde los tests con @DataJpaTest: cada test corre en una transacción que se deshace al terminar,
 * así que nada de lo que se inserta aquí queda guardado.
 * Los parámetros del SQL se escriben como ?1, ?2...
 */
public final class BD {

    private final EntityManager em;

    public BD(EntityManager em) {
        this.em = em;
    }

    private Query consulta(String sql, Object[] parametros) {
        Query consulta = em.createNativeQuery(sql);
        for (int i = 0; i < parametros.length; i++) {
            consulta.setParameter(i + 1, parametros[i]);
        }
        return consulta;
    }

    public void ejecutar(String sql, Object... parametros) {
        consulta(sql, parametros).executeUpdate();
    }

    public Object valor(String sql, Object... parametros) {
        return consulta(sql, parametros).getSingleResult();
    }

    public int numero(String sql, Object... parametros) {
        return ((Number) valor(sql, parametros)).intValue();
    }

    /** Inserta una fila y devuelve el id que le ha dado la base de datos. */
    public int insertar(String tabla, String sql, Object... parametros) {
        ejecutar(sql, parametros);
        return numero("SELECT currval(pg_get_serial_sequence('" + tabla + "', 'id'))");
    }

    /** Id de una opción de catálogo a partir de su código: idCatalogo("cat_dieta", "VEGANA"). */
    public int idCatalogo(String tabla, String codigo) {
        return numero("SELECT id FROM " + tabla + " WHERE codigo = ?1", codigo);
    }

    public int idPor(String tabla, String columna, String valor) {
        return numero("SELECT id FROM " + tabla + " WHERE " + columna + " = ?1", valor);
    }

    /** La fecha de hoy según la base de datos (las fechas relativas de los tests se calculan con ella, no con la del ordenador). */
    public LocalDate hoy() {
        Object fecha = valor("SELECT CURRENT_DATE");
        if (fecha instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        if (fecha instanceof LocalDate local) {
            return local;
        }
        return LocalDate.parse(fecha.toString().substring(0, 10));
    }

    // ------------------------------------------------------------------ atajos
    /** Crea un usuario "username" con email username@prueba.es y objetivo de 2000 kcal. */
    public int usuario(String username) {
        return insertar("usuario", "INSERT INTO usuario (email, username, password_hash, nombre, objetivo_calorias_dia) "
                + "VALUES (?1, ?2, 'hash', 'Prueba', 2000)", username + "@prueba.es", username);
    }

    public int despensa(int idUsuario, String nombre) {
        return insertar("despensa", "INSERT INTO despensa (id_usuario, nombre) VALUES (?1, ?2)", idUsuario, nombre);
    }

    /**
     * Añade 500 g de un ingrediente a una despensa.
     *
     * @param diasParaCaducar días desde hoy hasta la caducidad (negativo = ya caducado), o null si no caduca
     * @param dadoDeBaja      si es true, el producto sale de la despensa hoy como CONSUMIDO
     */
    public int producto(int idDespensa, String ingrediente, Integer diasParaCaducar, boolean dadoDeBaja) {
        String caducidad = diasParaCaducar == null ? "NULL" : "CURRENT_DATE + " + diasParaCaducar;
        String baja = dadoDeBaja ? "CURRENT_DATE" : "NULL";
        String motivo = dadoDeBaja ? "(SELECT id FROM cat_motivo_baja WHERE codigo = 'CONSUMIDO')" : "NULL";
        return insertar("despensa_item", "INSERT INTO despensa_item (id_despensa, id_ingrediente, cantidad, id_unidad, fecha_caducidad, fecha_baja, id_motivo_baja) "
                + "VALUES (?1, (SELECT id FROM ingrediente WHERE nombre = ?2), 500, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), "
                + caducidad + ", " + baja + ", " + motivo + ")", idDespensa, ingrediente);
    }
}
