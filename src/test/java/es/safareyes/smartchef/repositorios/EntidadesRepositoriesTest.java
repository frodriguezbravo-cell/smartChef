package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.DiarioCocina;
import es.safareyes.smartchef.modelos.DiarioFoto;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.RecetaPaso;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.UsuarioRecetaGuardada;
import es.safareyes.smartchef.modelos.catalogos.Dificultad;
import es.safareyes.smartchef.modelos.catalogos.TipoCocina;
import es.safareyes.smartchef.modelos.catalogos.TipoPlato;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guarda entidades a través de los repositorios (saveAndFlush) y las vuelve a leer.
 * Comprueba lo que los tests de consultas no ven: que cada entidad se puede INSERTAR con la base de datos real,
 * que las cascadas y el borrado de huérfanos funcionan, que la auditoría rellena creado_en / actualizado_en
 * y que las restricciones del SQL (claves únicas) se respetan.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EntidadesRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IUsuarioRepository usuarios;
    @Autowired private IRolUsuarioRepository roles;
    @Autowired private IUnidadTemperaturaRepository temperaturas;
    @Autowired private IUnidadVolumenRepository volumenes;
    @Autowired private IRecetaRepository recetas;
    @Autowired private IRecetaIngredienteRepository lineasReceta;
    @Autowired private IRecetaPasoRepository pasos;
    @Autowired private IIngredienteRepository ingredientes;
    @Autowired private IUnidadMedidaRepository unidades;
    @Autowired private IDespensaRepository despensas;
    @Autowired private IDespensaItemRepository productos;
    @Autowired private IUbicacionDespensaRepository ubicaciones;
    @Autowired private IMotivoBajaRepository motivos;
    @Autowired private IListaCompraRepository listas;
    @Autowired private IListaCompraItemRepository lineasLista;
    @Autowired private ITipoComidaRepository tiposComida;
    @Autowired private IPlanComidaRepository planes;
    @Autowired private IDiarioCocinaRepository diarios;
    @Autowired private IDiarioFotoRepository fotos;
    @Autowired private IAlergenoRepository alergenos;
    @Autowired private IGravedadRepository gravedades;
    @Autowired private IUsuarioAlergenoRepository usuarioAlergenos;
    @Autowired private IUsuarioRecetaGuardadaRepository guardadas;
    @Autowired private IListaGuardadaRepository listasGuardadas;

    private Usuario nuevoUsuario(String username) {
        Usuario u = new Usuario();
        u.setEmail(username + "@entidades.es");
        u.setUsername(username);
        u.setPasswordHash("hash");
        u.setRol(roles.findByCodigo("USUARIO").orElseThrow());
        u.setUnidadTemperatura(temperaturas.findByCodigo("C").orElseThrow());
        u.setUnidadVolumen(volumenes.findByCodigo("L").orElseThrow());
        return usuarios.saveAndFlush(u);
    }

    private Receta nuevaReceta(String slug) {
        Receta r = new Receta();
        r.setTitulo("Receta " + slug);
        r.setSlug(slug);
        r.setTipoPlato(em.find(TipoPlato.class, (short) bd().idCatalogo("cat_tipo_plato", "POSTRE")));
        r.setTipoCocina(em.find(TipoCocina.class, (short) bd().idCatalogo("cat_tipo_cocina", "INTERNACIONAL")));
        r.setDificultad(em.find(Dificultad.class, (short) bd().idCatalogo("cat_dificultad", "FACIL")));
        return r;
    }

    private RecetaIngrediente linea(Receta receta, String slugIngrediente, int orden) {
        RecetaIngrediente linea = new RecetaIngrediente();
        linea.setReceta(receta);
        linea.setIngrediente(ingredientes.findBySlug(slugIngrediente).orElseThrow());
        linea.setCantidad(new BigDecimal("250"));
        linea.setUnidad(unidades.findByCodigo("G").orElseThrow());
        linea.setCantidadGramos(new BigDecimal("250"));
        linea.setOrden((short) orden);
        return linea;
    }

    @Test
    void guardaUnUsuarioConAuditoriaYValoresPorDefecto() {
        Usuario guardado = nuevoUsuario("nuevo");
        assertNotNull(guardado.getId());
        assertNotNull(guardado.getCreadoEn());                 // lo rellena @CreatedDate (requiere @EnableJpaAuditing)
        assertNotNull(guardado.getActualizadoEn());

        em.clear();
        Usuario leido = usuarios.findById(guardado.getId()).orElseThrow();
        assertEquals("nuevo", leido.getUsername());
        assertEquals("es", leido.getIdioma());
        assertEquals(2, leido.getRacionesPorDefecto().intValue());
        assertTrue(leido.getActivo());
        assertEquals("USUARIO", leido.getRol().getCodigo());
    }

    @Test
    void guardaUnaRecetaConSusIngredientesYPasosEnCascada() {
        Receta receta = nuevaReceta("tarta-de-prueba");
        receta.getIngredientes().add(linea(receta, "harina-de-trigo", 2));
        receta.getIngredientes().add(linea(receta, "huevo", 1));
        RecetaPaso paso = new RecetaPaso();
        paso.setReceta(receta);
        paso.setNumero((short) 1);
        paso.setDescripcion("Mezclar todo");
        receta.getPasos().add(paso);

        Receta guardada = recetas.saveAndFlush(receta);
        assertNotNull(guardada.getId());
        assertNotNull(guardada.getCreadoEn());
        em.clear();

        Receta leida = recetas.findById(guardada.getId()).orElseThrow();
        assertEquals(2, leida.getIngredientes().size());
        assertEquals("Huevo", leida.getIngredientes().get(0).getIngrediente().getNombre());     // @OrderBy("orden ASC")
        assertEquals("Harina de trigo", leida.getIngredientes().get(1).getIngrediente().getNombre());
        assertEquals(1, leida.getPasos().size());
        assertEquals(2, lineasReceta.findByRecetaIdOrderByOrdenAsc(guardada.getId()).size());
        assertEquals(1, pasos.findByRecetaIdOrderByNumeroAsc(guardada.getId()).size());
    }

    @Test
    void alQuitarUnIngredienteDeLaRecetaSeBorraDeLaBaseDeDatos() {
        Receta receta = nuevaReceta("tarta-huerfanos");
        receta.getIngredientes().add(linea(receta, "harina-de-trigo", 1));
        receta.getIngredientes().add(linea(receta, "huevo", 2));
        Receta guardada = recetas.saveAndFlush(receta);
        assertEquals(2, lineasReceta.findByRecetaIdOrderByOrdenAsc(guardada.getId()).size());

        guardada.getIngredientes().remove(0);                                   // orphanRemoval = true
        recetas.saveAndFlush(guardada);
        assertEquals(1, lineasReceta.findByRecetaIdOrderByOrdenAsc(guardada.getId()).size());
    }

    @Test
    void dosRecetasNoPuedenTenerElMismoSlug() {
        recetas.saveAndFlush(nuevaReceta("slug-repetido"));
        Receta repetida = nuevaReceta("slug-repetido");
        assertThrows(DataIntegrityViolationException.class, () -> recetas.saveAndFlush(repetida));
    }

    @Test
    void guardaUnaDespensaConProductosYLosDaDeBaja() {
        Usuario usuario = nuevoUsuario("despensero");
        Despensa despensa = new Despensa();
        despensa.setUsuario(usuario);
        despensa.setNombre("Casa");
        despensa = despensas.saveAndFlush(despensa);
        assertNotNull(despensa.getCreadoEn());
        assertTrue(despensas.existsByUsuarioIdAndNombre(usuario.getId(), "Casa"));

        DespensaItem producto = new DespensaItem();
        producto.setDespensa(despensa);
        producto.setIngrediente(ingredientes.findBySlug("tomate").orElseThrow());
        producto.setCantidad(new BigDecimal("2"));
        producto.setUnidad(unidades.findByCodigo("UD").orElseThrow());
        producto.setUbicacion(ubicaciones.findByCodigo("NEVERA").orElseThrow());
        producto = productos.saveAndFlush(producto);
        assertNotNull(producto.getId());
        assertEquals(LocalDate.now(), producto.getFechaCompra());
        assertEquals(1, productos.findByDespensaIdAndFechaBajaIsNull(despensa.getId()).size());

        producto.setFechaBaja(producto.getFechaCompra());
        producto.setMotivoBaja(motivos.findByCodigo("CONSUMIDO").orElseThrow());
        productos.saveAndFlush(producto);
        assertTrue(productos.findByDespensaIdAndFechaBajaIsNull(despensa.getId()).isEmpty());
    }

    @Test
    void unaDespensaNoPuedeRepetirNombreParaElMismoUsuario() {
        Usuario usuario = nuevoUsuario("repetidor");
        Despensa primera = new Despensa();
        primera.setUsuario(usuario);
        primera.setNombre("Casa");
        despensas.saveAndFlush(primera);
        Despensa segunda = new Despensa();
        segunda.setUsuario(usuario);
        segunda.setNombre("Casa");
        assertThrows(DataIntegrityViolationException.class, () -> despensas.saveAndFlush(segunda));
    }

    @Test
    void guardaUnaListaDeLaCompraConSusLineasEnCascada() {
        Usuario usuario = nuevoUsuario("comprador");
        ListaCompra lista = new ListaCompra();
        lista.setUsuario(usuario);
        lista.setNombre("Semana");
        ListaCompraItem linea = new ListaCompraItem();
        linea.setLista(lista);
        linea.setIngrediente(ingredientes.findBySlug("tomate").orElseThrow());
        linea.setCantidad(new BigDecimal("500"));
        linea.setUnidad(unidades.findByCodigo("G").orElseThrow());
        ListaCompraItem libre = new ListaCompraItem();
        libre.setLista(lista);
        libre.setDescripcionLibre("Pilas para la báscula");
        lista.getItems().add(linea);
        lista.getItems().add(libre);

        ListaCompra guardada = listas.saveAndFlush(lista);
        assertNotNull(guardada.getCreadoEn());
        assertEquals(2, lineasLista.findByListaId(guardada.getId()).size());
        assertEquals(2, lineasLista.findByListaIdAndCompradoFalse(guardada.getId()).size());
        assertEquals(1, listas.findByUsuarioIdAndCompletadaFalse(usuario.getId()).size());

        guardada.getItems().clear();                                             // orphanRemoval = true
        listas.saveAndFlush(guardada);
        em.clear();
        assertTrue(lineasLista.findByListaId(guardada.getId()).isEmpty());
    }

    @Test
    void unaLineaDeListaSinIngredienteNiDescripcionLaRechazaLaBaseDeDatos() {
        Usuario usuario = nuevoUsuario("sinlinea");
        ListaCompra lista = new ListaCompra();
        lista.setUsuario(usuario);
        lista.setNombre("Vacía");
        lista.getItems().add(vacia(lista));
        assertThrows(DataIntegrityViolationException.class, () -> listas.saveAndFlush(lista));
    }

    private ListaCompraItem vacia(ListaCompra lista) {
        ListaCompraItem item = new ListaCompraItem();
        item.setLista(lista);
        return item;
    }

    @Test
    void guardaUnaComidaPlanificadaYApareceEnLosAvisos() {
        Usuario usuario = nuevoUsuario("planificador");
        PlanComida plan = new PlanComida();
        plan.setUsuario(usuario);
        plan.setFecha(LocalDate.now());
        plan.setTipoComida(tiposComida.findByCodigo("CENA").orElseThrow());
        plan.setDescripcionLibre("Cena fuera");
        plan = planes.saveAndFlush(plan);
        assertNotNull(plan.getCreadoEn());
        assertEquals(0, avisosDe(usuario));                                      // sin aviso: no aparece

        plan.setNotificar(true);
        planes.saveAndFlush(plan);
        assertEquals(1, avisosDe(usuario));                                      // con aviso y sin completar: aparece

        plan.setCompletada(true);
        planes.saveAndFlush(plan);
        assertEquals(0, avisosDe(usuario));                                      // ya cocinada: deja de avisar
    }

    private long avisosDe(Usuario usuario) {
        return planes.findByFechaAndNotificarTrueAndCompletadaFalse(LocalDate.now()).stream()
                .filter(p -> p.getUsuario().getId().equals(usuario.getId())).count();
    }

    @Test
    void guardaUnaEntradaDelDiarioConSusFotosEnCascada() {
        Usuario usuario = nuevoUsuario("cocinillas");
        DiarioCocina entrada = new DiarioCocina();
        entrada.setUsuario(usuario);
        entrada.setReceta(recetas.findBySlug("espaguetis-al-pomodoro").orElseThrow());
        entrada.setTitulo("Cena de ayer");
        entrada.setContenido("Salieron riquísimos");
        entrada.setValoracion((short) 5);
        DiarioFoto foto = new DiarioFoto();
        foto.setDiario(entrada);
        foto.setUrl("http://x/plato.jpg");
        entrada.getFotos().add(foto);

        DiarioCocina guardada = diarios.saveAndFlush(entrada);
        assertNotNull(guardada.getCreadoEn());
        assertNotNull(guardada.getFecha());
        assertEquals(1, diarios.findByUsuarioIdOrderByFechaDesc(usuario.getId()).size());
        assertEquals(1, fotos.findByDiarioId(guardada.getId()).size());
        assertNotNull(fotos.findByDiarioId(guardada.getId()).get(0).getTomadaEn());
    }

    @Test
    void guardaLasAlergiasDelUsuarioYNoPermiteRepetirlas() {
        Usuario usuario = nuevoUsuario("alergico");
        UsuarioAlergeno alergia = new UsuarioAlergeno();
        alergia.setUsuario(usuario);
        alergia.setAlergeno(alergenos.findByCodigo("GLUTEN").orElseThrow());
        alergia.setGravedad(gravedades.findByCodigo("ALERGIA_GRAVE").orElseThrow());
        alergia.setNotas("celíaco");
        alergia = usuarioAlergenos.saveAndFlush(alergia);
        assertNotNull(alergia.getCreadoEn());
        assertEquals(1, usuarioAlergenos.codigosDeAlergenos(usuario.getId()).size());

        UsuarioAlergeno repetida = new UsuarioAlergeno();
        repetida.setUsuario(usuario);
        repetida.setAlergeno(alergenos.findByCodigo("GLUTEN").orElseThrow());
        repetida.setGravedad(gravedades.findByCodigo("ALERGIA_LEVE").orElseThrow());
        assertThrows(DataIntegrityViolationException.class, () -> usuarioAlergenos.saveAndFlush(repetida));
    }

    @Test
    void guardaUnaRecetaFavoritaYNoPermiteDuplicarla() {
        Usuario usuario = nuevoUsuario("favorito");
        Receta receta = recetas.findBySlug("ensalada-de-garbanzos").orElseThrow();
        UsuarioRecetaGuardada guardada = new UsuarioRecetaGuardada();
        guardada.setUsuario(usuario);
        guardada.setReceta(receta);
        guardada.setLista(listasGuardadas.findByCodigo("FAVORITA").orElseThrow());
        guardada = guardadas.saveAndFlush(guardada);
        assertNotNull(guardada.getCreadoEn());
        assertEquals(1, guardadas.findByUsuarioIdAndListaCodigo(usuario.getId(), "FAVORITA").size());
        assertTrue(guardadas.existsByUsuarioIdAndRecetaIdAndListaId(usuario.getId(), receta.getId(), guardada.getLista().getId()));

        UsuarioRecetaGuardada duplicada = new UsuarioRecetaGuardada();
        duplicada.setUsuario(usuario);
        duplicada.setReceta(receta);
        duplicada.setLista(listasGuardadas.findByCodigo("FAVORITA").orElseThrow());
        assertThrows(DataIntegrityViolationException.class, () -> guardadas.saveAndFlush(duplicada));
    }
}
