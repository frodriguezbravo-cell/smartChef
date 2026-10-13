package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.MotivoBaja;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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

class DespensaServiceTest {

    private final Usuario usuario = usuario(1);
    private final Despensa despensa = new Despensa();
    private final Map<Integer, DespensaItem> productos = new HashMap<>();
    private final List<Object> limitesDeCaducidad = new ArrayList<>();
    private final Receta receta1 = Datos.receta("R1", 2);
    private final Receta receta2 = Datos.receta("R2", 2);
    private final DespensaService servicio;

    DespensaServiceTest() {
        despensa.setId(5);
        despensa.setUsuario(usuario);
        receta1.setId(1);
        receta2.setId(2);
        Ingrediente tomate = Datos.ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01");
        tomate.setDiasConservacion((short) 7);
        Ingrediente sal = Datos.ingrediente(9, "Sal", "0", "0", "0", "0", "0", "0", "0", "0");
        UnidadMedida gramos = Datos.unidad(1, "G", "gramos (g)");
        UbicacionDespensa alacena = opcion(new UbicacionDespensa(), 1, "ALACENA", "Alacena");
        UbicacionDespensa nevera = opcion(new UbicacionDespensa(), 2, "NEVERA", "Nevera");
        MotivoBaja consumido = opcion(new MotivoBaja(), 1, "CONSUMIDO", "Consumido");

        var despensas = metodos();
        despensas.put("findById", a -> a[0].equals(5) ? Optional.of(despensa) : Optional.empty());
        despensas.put("existsByUsuarioIdAndNombre", a -> a[1].equals("Casa"));
        var items = metodos();
        items.put("findById", a -> Optional.ofNullable(productos.get(a[0])));
        items.put("buscarQueCaducanAntesDe", a -> {
            limitesDeCaducidad.add(a[1]);
            return List.of();
        });
        var ingredientes = metodos();
        ingredientes.put("findById", a -> a[0].equals(2) ? Optional.of(tomate) : a[0].equals(9) ? Optional.of(sal) : Optional.empty());
        var unidades = metodos();
        unidades.put("findByCodigo", a -> a[0].equals("G") ? Optional.of(gramos) : Optional.empty());
        var ubicaciones = metodos();
        ubicaciones.put("findByCodigo", a -> a[0].equals("ALACENA") ? Optional.of(alacena)
                : a[0].equals("NEVERA") ? Optional.of(nevera) : Optional.empty());
        var motivos = metodos();
        motivos.put("findByCodigo", a -> a[0].equals("CONSUMIDO") ? Optional.of(consumido) : Optional.empty());
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var recetas = metodos();
        recetas.put("findByPublicadaTrue", a -> List.of(receta1, receta2));
        recetas.put("buscarCocinablesConDespensa", a -> List.of(receta1, receta2));

        RecetaService recetaService = new RecetaService(fake(IRecetaRepository.class, recetas),
                fake(IUsuarioRepository.class, usuarios), fake(IUsuarioAlergenoRepository.class));
        servicio = new DespensaService(fake(IDespensaRepository.class, despensas), fake(IDespensaItemRepository.class, items),
                fake(IUsuarioRepository.class, usuarios), fake(IIngredienteRepository.class, ingredientes),
                fake(IUnidadMedidaRepository.class, unidades), fake(IUbicacionDespensaRepository.class, ubicaciones),
                fake(IMotivoBajaRepository.class, motivos), fake(IRecetaRepository.class, recetas), recetaService);
    }

    @Test
    void sinUbicacionNiCaducidadSeUsanLosValoresPorDefecto() {
        DespensaItem item = servicio.anadirProducto(5, 2, new BigDecimal("500"), "G", null, null);
        assertEquals("ALACENA", item.getUbicacion().getCodigo());
        assertEquals(LocalDate.now().plusDays(7), item.getFechaCaducidad());   // 7 días de conservación del tomate
    }

    @Test
    void seRespetanLaUbicacionYLaCaducidadIndicadas() {
        DespensaItem item = servicio.anadirProducto(5, 2, BigDecimal.ONE, "G", "NEVERA", LocalDate.of(2030, 1, 1));
        assertEquals("NEVERA", item.getUbicacion().getCodigo());
        assertEquals(LocalDate.of(2030, 1, 1), item.getFechaCaducidad());
    }

    @Test
    void sinDiasDeConservacionNoSeInventaLaCaducidad() {
        assertNull(servicio.anadirProducto(5, 9, BigDecimal.ONE, "G", null, null).getFechaCaducidad());
    }

    @Test
    void rechazaDatosNoValidosAlAnadir() {
        assertThrows(ReglaNegocioException.class, () -> servicio.anadirProducto(5, 2, BigDecimal.ZERO, "G", null, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirProducto(5, 2, BigDecimal.ONE, "KILATES", null, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirProducto(77, 2, BigDecimal.ONE, "G", null, null));
    }

    @Test
    void noSePuedeRepetirNiDejarEnBlancoElNombreDeLaDespensa() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crearDespensa(1, "Casa"));
        assertThrows(ReglaNegocioException.class, () -> servicio.crearDespensa(1, "  "));
        assertEquals("Playa", servicio.crearDespensa(1, "  Playa ").getNombre());
    }

    @Test
    void darDeBajaGuardaLaFechaDeHoyYElMotivo() {
        DespensaItem item = productoComprado(LocalDate.now().minusDays(2));
        servicio.darDeBaja(1, "CONSUMIDO");
        assertEquals(LocalDate.now(), item.getFechaBaja());
        assertEquals("CONSUMIDO", item.getMotivoBaja().getCodigo());
        assertThrows(ReglaNegocioException.class, () -> servicio.darDeBaja(1, "CONSUMIDO"));   // ya estaba de baja
    }

    @Test
    void laBajaNuncaEsAnteriorALaCompra() {
        DespensaItem item = productoComprado(LocalDate.now().plusDays(3));      // la BD exige fecha_baja >= fecha_compra
        servicio.darDeBaja(1, "CONSUMIDO");
        assertFalse(item.getFechaBaja().isBefore(item.getFechaCompra()));
    }

    @Test
    void rechazaUnMotivoDeBajaQueNoExiste() {
        productoComprado(LocalDate.now());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.darDeBaja(1, "ROBADO"));
    }

    @Test
    void laCaducidadSeCalculaDesdeHoy() {
        assertThrows(ReglaNegocioException.class, () -> servicio.productosQueCaducan(1, -1));
        servicio.productosQueCaducan(1, 3);
        assertEquals(List.of(LocalDate.now().plusDays(3)), limitesDeCaducidad);
    }

    @Test
    void cocinablesSonLasDeLaDespensaQueTambienSonSeguras() {
        assertEquals(2, servicio.recetasCocinables(1, 5).size());
        usuario.getDietas().add(Datos.dieta("VEGANA"));       // ninguna receta está marcada como vegana
        assertTrue(servicio.recetasCocinables(1, 5).isEmpty());
    }

    @Test
    void unaDespensaAjenaSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.recetasCocinables(2, 5));
    }

    private DespensaItem productoComprado(LocalDate fechaCompra) {
        DespensaItem item = new DespensaItem();
        item.setFechaCompra(fechaCompra);
        productos.put(1, item);
        return item;
    }

    @Test
    void unaDespensaAjenaOInexistenteSeRespondeIgual() {
        assertSame(despensa, servicio.despensaDelUsuario(1, 5));
        RecursoNoEncontradoException ajena = assertThrows(RecursoNoEncontradoException.class, () -> servicio.despensaDelUsuario(2, 5));
        RecursoNoEncontradoException inexistente = assertThrows(RecursoNoEncontradoException.class, () -> servicio.despensaDelUsuario(1, 99));
        assertEquals("Despensa no encontrado/a: 5", ajena.getMessage());
        assertEquals("Despensa no encontrado/a: 99", inexistente.getMessage());
    }

    @Test
    void unProductoSoloSeEncuentraEnSuDespensaYDeSuDueno() {
        DespensaItem item = productoComprado(LocalDate.now());
        item.setDespensa(despensa);
        assertSame(item, servicio.productoDelUsuario(1, 5, 1));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.productoDelUsuario(2, 5, 1));    // otro usuario
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.productoDelUsuario(1, 5, 42));   // no existe
        Despensa otra = new Despensa();
        otra.setId(8);
        item.setDespensa(otra);
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.productoDelUsuario(1, 5, 1));    // está en otra despensa
    }
}
