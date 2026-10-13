package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DiarioCocina;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Listas de la compra, plan de comidas y diario de cocina. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ListaPlanDiarioRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IListaCompraRepository listas;
    @Autowired private IListaCompraItemRepository lineas;
    @Autowired private IPlanComidaRepository planes;
    @Autowired private IDiarioCocinaRepository diarios;
    @Autowired private IDiarioFotoRepository fotos;

    // ------------------------------------------------------------------ lista de la compra
    private int lista(int usuario, String nombre, boolean completada, int diasAtras) {
        return bd().insertar("lista_compra", "INSERT INTO lista_compra (id_usuario, nombre, completada, creado_en) VALUES (?1, ?2, "
                + (completada ? "TRUE" : "FALSE") + ", now() - interval '" + diasAtras + " day')", usuario, nombre);
    }

    private int linea(int lista, String ingrediente, boolean comprado) {
        return bd().insertar("lista_compra_item", "INSERT INTO lista_compra_item (id_lista, id_ingrediente, cantidad, id_unidad, comprado) VALUES (?1, "
                + "(SELECT id FROM ingrediente WHERE nombre = ?2), 500, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), "
                + (comprado ? "TRUE" : "FALSE") + ")", lista, ingrediente);
    }

    @Test
    void lasListasDelUsuarioVanDeLaMasNuevaALaMasAntigua() {
        int ana = bd().usuario("ana");
        lista(ana, "Antigua", false, 3);
        lista(ana, "Reciente", false, 1);
        lista(ana, "Intermedia", true, 2);
        lista(bd().usuario("luis"), "De Luis", false, 0);

        List<ListaCompra> resultado = listas.findByUsuarioIdOrderByCreadoEnDesc(ana);
        assertEquals(3, resultado.size());
        assertEquals("Reciente", resultado.get(0).getNombre());
        assertEquals("Intermedia", resultado.get(1).getNombre());
        assertEquals("Antigua", resultado.get(2).getNombre());
    }

    @Test
    void lasListasPendientesSonLasNoCompletadas() {
        int ana = bd().usuario("ana");
        lista(ana, "Pendiente", false, 1);
        lista(ana, "Hecha", true, 2);

        List<ListaCompra> pendientes = listas.findByUsuarioIdAndCompletadaFalse(ana);
        assertEquals(1, pendientes.size());
        assertEquals("Pendiente", pendientes.get(0).getNombre());
        assertTrue(listas.findByUsuarioIdAndCompletadaFalse(ana + 1000).isEmpty());
    }

    @Test
    void lasLineasDeUnaListaSeFiltranPorSiEstanCompradas() {
        int idLista = lista(bd().usuario("ana"), "Semana", false, 0);
        linea(idLista, "Tomate", false);
        linea(idLista, "Ajo", true);
        linea(idLista, "Cebolla", false);
        linea(lista(bd().usuario("luis"), "Otra", false, 0), "Huevo", false);

        List<ListaCompraItem> todas = lineas.findByListaId(idLista);
        assertEquals(3, todas.size());
        assertTrue(todas.stream().allMatch(l -> l.getLista().getId().equals(idLista)));

        List<ListaCompraItem> pendientes = lineas.findByListaIdAndCompradoFalse(idLista);
        assertEquals(2, pendientes.size());
        assertTrue(pendientes.stream().noneMatch(l -> l.getIngrediente().getNombre().equals("Ajo")));
        assertEquals("gramos (g)", pendientes.get(0).getUnidad().getNombre());
        assertTrue(lineas.findByListaId(idLista + 1000).isEmpty());
    }

    // ------------------------------------------------------------------ plan de comidas
    private int comida(int usuario, int dias, String tipo, boolean notificar, boolean completada) {
        return bd().insertar("plan_comida", "INSERT INTO plan_comida (id_usuario, fecha, id_tipo_comida, descripcion_libre, raciones, notificar, completada) VALUES (?1, "
                + "CURRENT_DATE + " + dias + ", (SELECT id FROM cat_tipo_comida WHERE codigo = ?2), ?3, 1, "
                + (notificar ? "TRUE" : "FALSE") + ", " + (completada ? "TRUE" : "FALSE") + ")", usuario, tipo, tipo + " " + dias);
    }

    @Test
    void elPlanDeUnRangoVaOrdenadoPorDiaYPorMomentoDelDia() {
        int ana = bd().usuario("ana");
        comida(ana, 0, "CENA", false, false);
        comida(ana, 1, "COMIDA", false, false);
        comida(ana, 0, "DESAYUNO", false, false);
        comida(ana, 10, "COMIDA", false, false);                                  // fuera del rango
        comida(ana, -1, "COMIDA", false, false);                                  // fuera del rango
        comida(bd().usuario("luis"), 0, "COMIDA", false, false);                  // de otro usuario

        LocalDate hoy = bd().hoy();
        List<PlanComida> semana = planes.findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(ana, hoy, hoy.plusDays(6));
        assertEquals(3, semana.size());
        assertEquals("DESAYUNO", semana.get(0).getTipoComida().getCodigo());
        assertEquals(hoy, semana.get(0).getFecha());
        assertEquals("CENA", semana.get(1).getTipoComida().getCodigo());
        assertEquals(hoy, semana.get(1).getFecha());
        assertEquals("COMIDA", semana.get(2).getTipoComida().getCodigo());
        assertEquals(hoy.plusDays(1), semana.get(2).getFecha());

        assertEquals(1, planes.findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(ana, hoy.plusDays(10), hoy.plusDays(10)).size());
        assertTrue(planes.findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(ana, hoy.plusDays(20), hoy.plusDays(30)).isEmpty());
    }

    @Test
    void lasComidasConAvisoPendienteSonLasNotificablesYSinCompletar() {
        int ana = bd().usuario("ana");
        comida(ana, 0, "COMIDA", true, false);                                    // sí
        comida(ana, 0, "CENA", true, true);                                       // ya hecha
        comida(ana, 0, "DESAYUNO", false, false);                                 // sin aviso
        comida(ana, 1, "COMIDA", true, false);                                    // otro día
        int luis = bd().usuario("luis");
        comida(luis, 0, "MERIENDA", true, false);                                 // sí (otro usuario)

        // la consulta es de TODOS los usuarios: nos quedamos con los de este test por si la base de datos tiene más datos
        List<PlanComida> avisos = planes.findByFechaAndNotificarTrueAndCompletadaFalse(bd().hoy()).stream()
                .filter(p -> p.getUsuario().getId().equals(ana) || p.getUsuario().getId().equals(luis)).toList();
        assertEquals(2, avisos.size());
        assertTrue(avisos.stream().allMatch(p -> p.getNotificar() && !p.getCompletada()));
        assertTrue(avisos.stream().anyMatch(p -> p.getTipoComida().getCodigo().equals("COMIDA")));
        assertTrue(avisos.stream().anyMatch(p -> p.getTipoComida().getCodigo().equals("MERIENDA")));
        assertTrue(planes.findByFechaAndNotificarTrueAndCompletadaFalse(bd().hoy().plusDays(30)).isEmpty());
    }

    // ------------------------------------------------------------------ diario de cocina
    private int entradaDiario(int usuario, Integer receta, String titulo, int diasAtras) {
        String idReceta = receta == null ? "NULL" : String.valueOf(receta);
        return bd().insertar("diario_cocina", "INSERT INTO diario_cocina (id_usuario, id_receta, fecha, titulo, contenido, valoracion) VALUES (?1, "
                + idReceta + ", now() - interval '" + diasAtras + " day', ?2, 'Salió bien', 5)", usuario, titulo);
    }

    @Test
    void elDiarioVaDeLaEntradaMasRecienteALaMasAntigua() {
        int ana = bd().usuario("ana");
        entradaDiario(ana, null, "Hace tres días", 3);
        entradaDiario(ana, null, "Ayer", 1);
        entradaDiario(ana, null, "Hace dos días", 2);
        entradaDiario(bd().usuario("luis"), null, "De Luis", 0);

        List<DiarioCocina> resultado = diarios.findByUsuarioIdOrderByFechaDesc(ana);
        assertEquals(3, resultado.size());
        assertEquals("Ayer", resultado.get(0).getTitulo());
        assertEquals("Hace dos días", resultado.get(1).getTitulo());
        assertEquals("Hace tres días", resultado.get(2).getTitulo());
        assertEquals(5, resultado.get(0).getValoracion().intValue());
    }

    @Test
    void lasEntradasSeBuscanPorReceta() {
        int ana = bd().usuario("ana");
        int espaguetis = bd().idPor("receta", "slug", "espaguetis-al-pomodoro");
        entradaDiario(ana, espaguetis, "Con espaguetis", 1);
        entradaDiario(ana, null, "Sin receta", 1);

        List<DiarioCocina> resultado = diarios.findByRecetaId(espaguetis);
        assertEquals(1, resultado.size());
        assertEquals("Con espaguetis", resultado.get(0).getTitulo());
        assertEquals("espaguetis-al-pomodoro", resultado.get(0).getReceta().getSlug());
        assertTrue(diarios.findByRecetaId(bd().idPor("receta", "slug", "ensalada-de-garbanzos")).isEmpty());
    }

    @Test
    void lasFotosSeBuscanPorEntradaDelDiario() {
        int ana = bd().usuario("ana");
        int conFotos = entradaDiario(ana, null, "Con fotos", 1);
        int sinFotos = entradaDiario(ana, null, "Sin fotos", 2);
        bd().ejecutar("INSERT INTO diario_foto (id_diario, url, descripcion) VALUES (?1, 'http://x/1.jpg', 'La primera')", conFotos);
        bd().ejecutar("INSERT INTO diario_foto (id_diario, url) VALUES (?1, 'http://x/2.jpg')", conFotos);

        assertEquals(2, fotos.findByDiarioId(conFotos).size());
        assertTrue(fotos.findByDiarioId(conFotos).stream().anyMatch(f -> "La primera".equals(f.getDescripcion())));
        assertNotNull(fotos.findByDiarioId(conFotos).get(0).getTomadaEn());
        assertTrue(fotos.findByDiarioId(sinFotos).isEmpty());
    }
}
