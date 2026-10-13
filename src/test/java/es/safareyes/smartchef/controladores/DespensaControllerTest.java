package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.BajaProductoPeticion;
import es.safareyes.smartchef.controladores.dto.DespensaDto;
import es.safareyes.smartchef.controladores.dto.NuevaDespensaPeticion;
import es.safareyes.smartchef.controladores.dto.NuevoProductoPeticion;
import es.safareyes.smartchef.controladores.dto.ProductoDto;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.MotivoBaja;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import es.safareyes.smartchef.repositorios.IMotivoBajaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.DespensaService;
import es.safareyes.smartchef.servicios.RecetaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class DespensaControllerTest {

    private final Usuario usuario = usuario(1);
    private final Despensa despensa = new Despensa();
    private final Map<Integer, DespensaItem> productos = new HashMap<>();
    private final DespensaController controlador;

    DespensaControllerTest() {
        despensa.setId(5);
        despensa.setNombre("Casa");
        despensa.setUsuario(usuario);
        Ingrediente tomate = Datos.ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01");
        tomate.setDiasConservacion((short) 7);
        UbicacionDespensa alacena = opcion(new UbicacionDespensa(), 1, "ALACENA", "Alacena");
        MotivoBaja consumido = opcion(new MotivoBaja(), 1, "CONSUMIDO", "Consumido");

        var despensas = metodos();
        despensas.put("findById", a -> a[0].equals(5) ? Optional.of(despensa) : Optional.empty());
        despensas.put("findByUsuarioId", a -> List.of(despensa));
        var items = metodos();
        items.put("findById", a -> Optional.ofNullable(productos.get(a[0])));
        items.put("findByDespensaIdAndFechaBajaIsNull", a -> List.copyOf(productos.values()));
        var ingredientes = metodos();
        ingredientes.put("findById", a -> Optional.of(tomate));
        var unidades = metodos();
        unidades.put("findByCodigo", a -> Optional.of(Datos.unidad(1, "G", "gramos (g)")));
        var ubicaciones = metodos();
        ubicaciones.put("findByCodigo", a -> Optional.of(alacena));
        var motivos = metodos();
        motivos.put("findByCodigo", a -> Optional.of(consumido));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));

        RecetaService recetaService = new RecetaService(fake(IRecetaRepository.class), fake(IUsuarioRepository.class, usuarios),
                fake(IUsuarioAlergenoRepository.class));
        controlador = new DespensaController(new DespensaService(fake(IDespensaRepository.class, despensas),
                fake(IDespensaItemRepository.class, items), fake(IUsuarioRepository.class, usuarios), fake(IIngredienteRepository.class, ingredientes),
                fake(IUnidadMedidaRepository.class, unidades), fake(IUbicacionDespensaRepository.class, ubicaciones),
                fake(IMotivoBajaRepository.class, motivos), fake(IRecetaRepository.class), recetaService));
    }

    @Test
    void creaYListaLasDespensasDelUsuario() {
        assertEquals("Playa", controlador.crear(1, new NuevaDespensaPeticion("Playa")).nombre());
        List<DespensaDto> lista = controlador.despensas(1);
        assertEquals(1, lista.size());
        assertEquals("Casa", lista.get(0).nombre());
    }

    @Test
    void alAnadirUnProductoSeCalculanLaCaducidadYLosDiasRestantes() {
        ProductoDto producto = controlador.anadirProducto(1, 5, new NuevoProductoPeticion(2, new BigDecimal("500"), "G", null, null));
        assertEquals("Tomate", producto.ingrediente());
        assertEquals("Alacena", producto.ubicacion());
        assertEquals(LocalDate.now().plusDays(7), producto.fechaCaducidad());
        assertEquals(7L, producto.diasParaCaducar());
    }

    @Test
    void unaDespensaAjenaDaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.productos(2, 5));
        assertThrows(RecursoNoEncontradoException.class,
                () -> controlador.anadirProducto(2, 5, new NuevoProductoPeticion(2, BigDecimal.ONE, "G", null, null)));
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.darDeBaja(2, 5, 1, new BajaProductoPeticion("CONSUMIDO")));
    }

    @Test
    void darDeBajaDevuelveElProductoConSuMotivo() {
        DespensaItem item = new DespensaItem();
        item.setId(1);
        item.setDespensa(despensa);
        item.setIngrediente(Datos.ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01"));
        item.setFechaCompra(LocalDate.now().minusDays(1));
        productos.put(1, item);

        ProductoDto baja = controlador.darDeBaja(1, 5, 1, new BajaProductoPeticion("CONSUMIDO"));
        assertEquals("Consumido", baja.motivoBaja());
        assertEquals(LocalDate.now(), baja.fechaBaja());
    }

    @Test
    void unProductoQueCaducoTieneDiasNegativos() {
        DespensaItem item = new DespensaItem();
        item.setIngrediente(Datos.ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01"));
        item.setFechaCaducidad(LocalDate.now().minusDays(2));
        assertEquals(-2L, Mapeadores.producto(item).diasParaCaducar());
        item.setFechaCaducidad(null);
        assertNull(Mapeadores.producto(item).diasParaCaducar());
    }
}
