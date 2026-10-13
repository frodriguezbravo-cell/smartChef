package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DispositivoPush;
import es.safareyes.smartchef.modelos.TokenRefresco;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Usuarios, sesiones, dispositivos y las preferencias del usuario (alergias, utensilios, recetas guardadas). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IUsuarioRepository usuarios;
    @Autowired private ITokenRefrescoRepository tokens;
    @Autowired private IDispositivoPushRepository dispositivos;
    @Autowired private IUsuarioAlergenoRepository alergias;
    @Autowired private IUsuarioUtensilioRepository utensilios;
    @Autowired private IUsuarioRecetaGuardadaRepository guardadas;

    @Test
    void buscaUsuariosPorEmailYPorUsername() {
        int id = bd().usuario("ana");
        Usuario ana = usuarios.findByEmail("ana@prueba.es").orElseThrow();
        assertEquals(id, ana.getId());
        assertEquals("ana", ana.getUsername());
        assertEquals("ana", usuarios.findByUsername("ana").orElseThrow().getUsername());
        assertTrue(usuarios.findByEmail("nadie@prueba.es").isEmpty());
        assertTrue(usuarios.findByUsername("nadie").isEmpty());
    }

    @Test
    void losValoresPorDefectoDeLaBaseDeDatosSeLeenBien() {
        bd().usuario("luis");
        Usuario luis = usuarios.findByUsername("luis").orElseThrow();
        assertEquals("USUARIO", luis.getRol().getCodigo());
        assertEquals("C", luis.getUnidadTemperatura().getCodigo());
        assertEquals("L", luis.getUnidadVolumen().getCodigo());
        assertEquals("es", luis.getIdioma());
        assertEquals(2, luis.getRacionesPorDefecto().intValue());
        assertEquals(2000, luis.getObjetivoCaloriasDia());
        assertTrue(luis.getActivo());
        assertFalse(luis.getEmailVerificado());
        assertNotNull(luis.getCreadoEn());
    }

    @Test
    void compruebaSiExisteUnEmailOUnUsername() {
        bd().usuario("eva");
        assertTrue(usuarios.existsByEmail("eva@prueba.es"));
        assertFalse(usuarios.existsByEmail("otra@prueba.es"));
        assertTrue(usuarios.existsByUsername("eva"));
        assertFalse(usuarios.existsByUsername("otra"));
    }

    @Test
    void losTokensSeBuscanPorHashYSoloSeListanLosNoRevocados() {
        int idUsuario = bd().usuario("bea");
        bd().ejecutar("INSERT INTO token_refresco (id_usuario, token_hash, dispositivo, expira_en, revocado) "
                + "VALUES (?1, 'hash-1', 'Pixel', now() + interval '1 day', FALSE)", idUsuario);
        bd().ejecutar("INSERT INTO token_refresco (id_usuario, token_hash, dispositivo, expira_en, revocado) "
                + "VALUES (?1, 'hash-2', 'iPhone', now() + interval '1 day', TRUE)", idUsuario);

        TokenRefresco token = tokens.findByTokenHash("hash-1").orElseThrow();
        assertEquals("Pixel", token.getDispositivo());
        assertFalse(token.getRevocado());
        assertTrue(tokens.findByTokenHash("hash-2").orElseThrow().getRevocado());
        assertTrue(tokens.findByTokenHash("no-existe").isEmpty());

        List<TokenRefresco> vigentes = tokens.findByUsuarioIdAndRevocadoFalse(idUsuario);
        assertEquals(1, vigentes.size());
        assertEquals("hash-1", vigentes.get(0).getTokenHash());
        assertTrue(tokens.findByUsuarioIdAndRevocadoFalse(idUsuario + 1000).isEmpty());
    }

    @Test
    void losDispositivosPushSeBuscanPorTokenYSoloSeListanLosActivos() {
        int idUsuario = bd().usuario("carlos");
        bd().ejecutar("INSERT INTO dispositivo_push (id_usuario, token_push, id_plataforma, activo) "
                + "VALUES (?1, 'fcm-1', (SELECT id FROM cat_plataforma WHERE codigo = 'ANDROID'), TRUE)", idUsuario);
        bd().ejecutar("INSERT INTO dispositivo_push (id_usuario, token_push, id_plataforma, activo) "
                + "VALUES (?1, 'fcm-2', (SELECT id FROM cat_plataforma WHERE codigo = 'IOS'), FALSE)", idUsuario);

        List<DispositivoPush> activos = dispositivos.findByUsuarioIdAndActivoTrue(idUsuario);
        assertEquals(1, activos.size());
        assertEquals("fcm-1", activos.get(0).getTokenPush());
        assertEquals("ANDROID", activos.get(0).getPlataforma().getCodigo());
        assertFalse(dispositivos.findByTokenPush("fcm-2").orElseThrow().getActivo());
        assertTrue(dispositivos.findByTokenPush("no-existe").isEmpty());
    }

    @Test
    void lasAlergiasDelUsuarioSeConsultanDeVariasFormas() {
        int idUsuario = bd().usuario("dani");
        int idGluten = bd().idCatalogo("cat_alergeno", "GLUTEN");
        bd().ejecutar("INSERT INTO usuario_alergeno (id_usuario, id_alergeno, id_gravedad, notas) VALUES (?1, ?2, "
                + "(SELECT id FROM cat_gravedad WHERE codigo = 'ALERGIA_GRAVE'), 'celíaco')", idUsuario, idGluten);
        bd().ejecutar("INSERT INTO usuario_alergeno (id_usuario, id_alergeno, id_gravedad) VALUES (?1, "
                + "(SELECT id FROM cat_alergeno WHERE codigo = 'LACTEOS'), (SELECT id FROM cat_gravedad WHERE codigo = 'INTOLERANCIA'))", idUsuario);

        List<UsuarioAlergeno> todas = alergias.findByUsuarioId(idUsuario);
        assertEquals(2, todas.size());

        UsuarioAlergeno gluten = alergias.findByUsuarioIdAndAlergenoId(idUsuario, (short) idGluten).orElseThrow();
        assertEquals("GLUTEN", gluten.getAlergeno().getCodigo());
        assertEquals("ALERGIA_GRAVE", gluten.getGravedad().getCodigo());
        assertEquals("celíaco", gluten.getNotas());
        assertTrue(alergias.findByUsuarioIdAndAlergenoId(idUsuario, (short) bd().idCatalogo("cat_alergeno", "PESCADO")).isEmpty());

        assertTrue(alergias.existsByUsuarioIdAndAlergenoId(idUsuario, (short) idGluten));
        assertFalse(alergias.existsByUsuarioIdAndAlergenoId(idUsuario, (short) bd().idCatalogo("cat_alergeno", "SOJA")));

        List<String> codigos = alergias.codigosDeAlergenos(idUsuario);
        assertEquals(2, codigos.size());
        assertTrue(codigos.contains("GLUTEN"));
        assertTrue(codigos.contains("LACTEOS"));
        assertTrue(alergias.codigosDeAlergenos(idUsuario + 1000).isEmpty());
    }

    @Test
    void losUtensiliosDelUsuarioSonSoloLosSuyos() {
        int idUsuario = bd().usuario("elena");
        int otro = bd().usuario("fran");
        bd().ejecutar("INSERT INTO usuario_utensilio (id_usuario, id_utensilio, cantidad) VALUES (?1, "
                + "(SELECT id FROM utensilio WHERE modelo = 'Olla alta 24 cm'), 2)", idUsuario);
        bd().ejecutar("INSERT INTO usuario_utensilio (id_usuario, descripcion_libre) VALUES (?1, 'Mortero de piedra')", otro);

        assertEquals(1, utensilios.findByUsuarioId(idUsuario).size());
        assertEquals(2, utensilios.findByUsuarioId(idUsuario).get(0).getCantidad().intValue());
        assertEquals("Mortero de piedra", utensilios.findByUsuarioId(otro).get(0).getDescripcionLibre());
        assertTrue(utensilios.findByUsuarioId(otro + 1000).isEmpty());
    }

    @Test
    void lasRecetasGuardadasSeFiltranPorLista() {
        int idUsuario = bd().usuario("gema");
        int idReceta = bd().idPor("receta", "slug", "espaguetis-al-pomodoro");
        int idFavorita = bd().idCatalogo("cat_lista_guardada", "FAVORITA");
        bd().ejecutar("INSERT INTO usuario_receta_guardada (id_usuario, id_receta, id_lista) VALUES (?1, ?2, ?3)", idUsuario, idReceta, idFavorita);

        assertEquals(1, guardadas.findByUsuarioIdAndListaCodigo(idUsuario, "FAVORITA").size());
        assertEquals("espaguetis-al-pomodoro", guardadas.findByUsuarioIdAndListaCodigo(idUsuario, "FAVORITA").get(0).getReceta().getSlug());
        assertTrue(guardadas.findByUsuarioIdAndListaCodigo(idUsuario, "PARA_COCINAR").isEmpty());

        assertTrue(guardadas.existsByUsuarioIdAndRecetaIdAndListaId(idUsuario, idReceta, (short) idFavorita));
        assertFalse(guardadas.existsByUsuarioIdAndRecetaIdAndListaId(idUsuario, idReceta,
                (short) bd().idCatalogo("cat_lista_guardada", "PARA_COCINAR")));
        assertFalse(guardadas.existsByUsuarioIdAndRecetaIdAndListaId(idUsuario + 1000, idReceta, (short) idFavorita));
    }
}
