package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AnadirRecetaPeticion;
import es.safareyes.smartchef.controladores.dto.CompradoPeticion;
import es.safareyes.smartchef.controladores.dto.LineaListaDto;
import es.safareyes.smartchef.controladores.dto.ListaDetalleDto;
import es.safareyes.smartchef.controladores.dto.ListaDto;
import es.safareyes.smartchef.controladores.dto.NuevaListaPeticion;
import es.safareyes.smartchef.controladores.dto.TrasladoPeticion;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IListaCompraItemRepository;
import es.safareyes.smartchef.repositorios.IListaCompraRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.ListaCompraService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class ListaCompraControllerTest {

    private final Usuario usuario = usuario(1);
    private final Receta espaguetis = Datos.espaguetisAlPomodoro();
    private final Map<Integer, ListaCompra> listas = new HashMap<>();
    private final Map<Integer, ListaCompraItem> lineas = new HashMap<>();
    private final ListaCompra lista = new ListaCompra();
    private final ListaCompraController controlador;

    ListaCompraControllerTest() {
        espaguetis.setId(7);
        espaguetis.setTitulo("Espaguetis al pomodoro");
        lista.setId(3);
        lista.setNombre("Semana");
        lista.setUsuario(usuario);
        listas.put(3, lista);
        Despensa despensa = new Despensa();
        despensa.setId(5);
        despensa.setUsuario(usuario);
        UbicacionDespensa alacena = opcion(new UbicacionDespensa(), 1, "ALACENA", "Alacena");

        var listasRepo = metodos();
        listasRepo.put("findById", a -> Optional.ofNullable(listas.get(a[0])));
        listasRepo.put("findByUsuarioIdOrderByCreadoEnDesc", a -> List.copyOf(listas.values()));
        var lineasRepo = metodos();
        lineasRepo.put("findById", a -> Optional.ofNullable(lineas.get(a[0])));
        var recetas = metodos();
        recetas.put("findById", a -> Optional.of(espaguetis));
        var despensas = metodos();
        despensas.put("findById", a -> Optional.of(despensa));
        var ubicaciones = metodos();
        ubicaciones.put("findByCodigo", a -> Optional.of(alacena));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        controlador = new ListaCompraController(new ListaCompraService(fake(IListaCompraRepository.class, listasRepo),
                fake(IListaCompraItemRepository.class, lineasRepo), fake(IUsuarioRepository.class, usuarios),
                fake(IRecetaRepository.class, recetas), fake(IDespensaRepository.class, despensas),
                fake(IDespensaItemRepository.class), fake(IUbicacionDespensaRepository.class, ubicaciones)));
    }

    @Test
    void creaYListaLasListas() {
        assertEquals("Fin de semana", controlador.crear(1, new NuevaListaPeticion("Fin de semana")).nombre());
        assertFalse(controlador.listas(1).isEmpty());
    }

    @Test
    void anadirUnaRecetaDevuelveLaListaConSusLineas() {
        ListaDetalleDto detalle = controlador.anadirReceta(1, 3, new AnadirRecetaPeticion(7, 4, null));
        assertEquals(4, detalle.lineas().size());
        LineaListaDto tomate = detalle.lineas().stream().filter(l -> l.descripcion().equals("Tomate")).findFirst().orElseThrow();
        assertEquals("800.00", tomate.cantidad().toPlainString());
        assertEquals("gramos (g)", tomate.unidad());
        assertEquals("Espaguetis al pomodoro", tomate.receta());
        assertFalse(tomate.comprado());
    }

    @Test
    void elResumenDeLaListaContabilizaLoPendiente() {
        controlador.anadirReceta(1, 3, new AnadirRecetaPeticion(7, 2, null));
        lista.getItems().get(0).setComprado(true);
        ListaDto dto = Mapeadores.lista(lista);
        assertEquals(4, dto.totalLineas());
        assertEquals(3, dto.pendientes());
    }

    @Test
    void marcarCompradoCambiaLaLinea() {
        controlador.anadirReceta(1, 3, new AnadirRecetaPeticion(7, 2, null));
        ListaCompraItem linea = lista.getItems().get(0);
        linea.setId(9);
        lineas.put(9, linea);
        assertTrue(controlador.marcarComprado(1, 3, 9, new CompradoPeticion(true)).comprado());
    }

    @Test
    void trasladarCompruebaQueLaListaSeaDelUsuario() {
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.trasladarComprados(2, 3, new TrasladoPeticion(5)));
        assertEquals(0, controlador.trasladarComprados(1, 3, new TrasladoPeticion(5)).trasladados());
    }

    @Test
    void unaListaOUnaLineaAjenaDaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.detalle(2, 3));
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.anadirReceta(2, 3, new AnadirRecetaPeticion(7, 2, null)));
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.marcarComprado(2, 3, 9, new CompradoPeticion(true)));
    }
}
