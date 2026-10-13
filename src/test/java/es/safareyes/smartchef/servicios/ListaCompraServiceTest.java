package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IListaCompraItemRepository;
import es.safareyes.smartchef.repositorios.IListaCompraRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class ListaCompraServiceTest {

    private final Usuario usuario = usuario(1);
    private final Receta espaguetis = Datos.espaguetisAlPomodoro();       // 2 raciones; el parmesano es opcional
    private final UnidadMedida gramos = Datos.unidad(1, "G", "gramos (g)");
    private final List<DespensaItem> stockEnDespensa = new ArrayList<>();
    private final List<DespensaItem> guardadosEnDespensa = new ArrayList<>();
    private final Map<Integer, ListaCompra> listas = new HashMap<>();
    private final Map<Integer, ListaCompraItem> lineas = new HashMap<>();
    private final ListaCompra lista = new ListaCompra();
    private final ListaCompraService servicio;

    ListaCompraServiceTest() {
        espaguetis.setId(7);
        lista.setId(3);
        lista.setUsuario(usuario);
        listas.put(3, lista);
        Despensa despensa = new Despensa();
        despensa.setId(5);
        despensa.setUsuario(usuario);
        Despensa ajena = new Despensa();
        ajena.setId(6);
        ajena.setUsuario(usuario(2));
        UbicacionDespensa alacena = opcion(new UbicacionDespensa(), 1, "ALACENA", "Alacena");

        var listasRepo = metodos();
        listasRepo.put("findById", a -> Optional.ofNullable(listas.get(a[0])));
        var lineasRepo = metodos();
        lineasRepo.put("findById", a -> Optional.ofNullable(lineas.get(a[0])));
        var recetas = metodos();
        recetas.put("findById", a -> a[0].equals(7) ? Optional.of(espaguetis) : Optional.empty());
        var despensas = metodos();
        despensas.put("findById", a -> a[0].equals(5) ? Optional.of(despensa) : a[0].equals(6) ? Optional.of(ajena) : Optional.empty());
        var items = metodos();
        items.put("findByDespensaIdAndFechaBajaIsNull", a -> stockEnDespensa);
        items.put("save", a -> {
            guardadosEnDespensa.add((DespensaItem) a[0]);
            return a[0];
        });
        var ubicaciones = metodos();
        ubicaciones.put("findByCodigo", a -> Optional.of(alacena));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));

        servicio = new ListaCompraService(fake(IListaCompraRepository.class, listasRepo), fake(IListaCompraItemRepository.class, lineasRepo),
                fake(IUsuarioRepository.class, usuarios), fake(IRecetaRepository.class, recetas),
                fake(IDespensaRepository.class, despensas), fake(IDespensaItemRepository.class, items),
                fake(IUbicacionDespensaRepository.class, ubicaciones));
    }

    @Test
    void sinDespensaSeAnadeTodoLoObligatorioConLasCantidadesEscaladas() {
        servicio.anadirReceta(3, 7, 4, null);                  // el doble de la receta
        assertEquals(4, lista.getItems().size());              // el parmesano es opcional y no entra
        assertEquals("800.00", cantidad("Tomate"));
        assertEquals("400.00", cantidad("Espaguetis"));
    }

    @Test
    void anadirLaMismaRecetaOtraVezSumaEnLugarDeDuplicar() {
        servicio.anadirReceta(3, 7, 4, null);
        servicio.anadirReceta(3, 7, 2, null);
        assertEquals(4, lista.getItems().size());
        assertEquals("1200.00", cantidad("Tomate"));
        assertEquals("600.00", cantidad("Espaguetis"));
    }

    @Test
    void siHayDeSobraEnLaDespensaNoSeAnade() {
        stockEnDespensa.add(Datos.stock(tomate(), "1000", gramos));
        servicio.anadirReceta(3, 7, 4, 5);
        assertEquals(3, lista.getItems().size());
        assertEquals("(no está)", cantidad("Tomate"));
    }

    @Test
    void siFaltaPocoSoloSeAnadeLoQueFalta() {
        stockEnDespensa.add(Datos.stock(tomate(), "300", gramos));
        servicio.anadirReceta(3, 7, 4, 5);                     // necesita 800 y hay 300
        assertEquals("500.00", cantidad("Tomate"));
    }

    @Test
    void rechazaDatosNoValidos() {
        assertThrows(ReglaNegocioException.class, () -> servicio.anadirReceta(3, 7, 2, 6));     // despensa de otro usuario
        assertThrows(ReglaNegocioException.class, () -> servicio.anadirReceta(3, 7, 0, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirReceta(99, 7, 2, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirReceta(3, 99, 2, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.crearLista(1, ""));
    }

    @Test
    void loCompradoPasaALaDespensaYSaleDeLaLista() {
        servicio.anadirReceta(3, 7, 4, null);
        marcarComprado("Tomate");
        marcarComprado("Espaguetis");

        assertEquals(2, servicio.pasarCompradosADespensa(3, 5));
        assertEquals(2, guardadosEnDespensa.size());
        assertEquals("ALACENA", guardadosEnDespensa.get(0).getUbicacion().getCodigo());
        assertEquals(2, lista.getItems().size());              // lo pendiente sigue en la lista
        assertFalse(Boolean.TRUE.equals(lista.getCompletada()));
    }

    @Test
    void cuandoSeCompraTodoLaListaQuedaCompletada() {
        servicio.anadirReceta(3, 7, 4, null);
        lista.getItems().forEach(linea -> linea.setComprado(true));

        servicio.pasarCompradosADespensa(3, 5);
        assertTrue(lista.getItems().isEmpty());
        assertTrue(lista.getCompletada());
    }

    @Test
    void noSePuedeTrasladarADespensaAjena() {
        assertThrows(ReglaNegocioException.class, () -> servicio.pasarCompradosADespensa(3, 6));
    }

    private Ingrediente tomate() {
        return Datos.ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01");
    }

    private String cantidad(String ingrediente) {
        return lista.getItems().stream()
                .filter(linea -> linea.getIngrediente().getNombre().equals(ingrediente))
                .map(linea -> linea.getCantidad().toPlainString())
                .findFirst().orElse("(no está)");
    }

    private void marcarComprado(String ingrediente) {
        lista.getItems().stream()
                .filter((ListaCompraItem linea) -> linea.getIngrediente().getNombre().equals(ingrediente))
                .forEach(linea -> linea.setComprado(true));
    }

    @Test
    void unaListaAjenaSeTrataComoSiNoExistiera() {
        assertSame(lista, servicio.listaDelUsuario(1, 3));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.listaDelUsuario(2, 3));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.listaDelUsuario(1, 99));
    }

    @Test
    void unaLineaSoloSeEncuentraEnSuListaYDeSuDueno() {
        ListaCompraItem linea = new ListaCompraItem();
        linea.setId(4);
        linea.setLista(lista);
        lineas.put(4, linea);
        assertSame(linea, servicio.lineaDeLista(1, 3, 4));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.lineaDeLista(2, 3, 4));       // otro usuario
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.lineaDeLista(1, 3, 40));      // no existe
        ListaCompra otra = new ListaCompra();
        otra.setId(9);
        otra.setUsuario(usuario);
        listas.put(9, otra);
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.lineaDeLista(1, 9, 4));       // es de otra lista
    }
}
