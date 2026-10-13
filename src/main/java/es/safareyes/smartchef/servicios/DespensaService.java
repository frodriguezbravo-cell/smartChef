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
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import es.safareyes.smartchef.repositorios.IMotivoBajaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DespensaService {

    private static final String UBICACION_POR_DEFECTO = "ALACENA";

    private final IDespensaRepository despensaRepository;
    private final IDespensaItemRepository itemRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IIngredienteRepository ingredienteRepository;
    private final IUnidadMedidaRepository unidadRepository;
    private final IUbicacionDespensaRepository ubicacionRepository;
    private final IMotivoBajaRepository motivoBajaRepository;
    private final IRecetaRepository recetaRepository;
    private final RecetaService recetaService;

    public List<Despensa> despensasDe(Integer idUsuario) {
        return despensaRepository.findByUsuarioId(idUsuario);
    }

    @Transactional
    public Despensa crearDespensa(Integer idUsuario, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("La despensa necesita un nombre");
        }
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        String limpio = nombre.trim();
        if (despensaRepository.existsByUsuarioIdAndNombre(idUsuario, limpio)) {
            throw new ReglaNegocioException("Ya tienes una despensa llamada \"" + limpio + "\"");
        }
        Despensa despensa = new Despensa();
        despensa.setUsuario(usuario);
        despensa.setNombre(limpio);
        return despensaRepository.save(despensa);
    }

    /**
     * Guarda un producto en la despensa.
     *
     * @param codigoUbicacion  ALACENA, NEVERA o CONGELADOR (null = ALACENA)
     * @param fechaCaducidad   si es null y el ingrediente tiene días de conservación, se calcula desde hoy
     */
    @Transactional
    public DespensaItem anadirProducto(Integer idDespensa, Integer idIngrediente, BigDecimal cantidad,
                                       String codigoUnidad, String codigoUbicacion, LocalDate fechaCaducidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que 0");
        }
        Despensa despensa = Comprobaciones.existe(despensaRepository.findById(idDespensa), "Despensa", idDespensa);
        Ingrediente ingrediente = Comprobaciones.existe(ingredienteRepository.findById(idIngrediente), "Ingrediente", idIngrediente);
        UnidadMedida unidad = Comprobaciones.existe(unidadRepository.findByCodigo(codigoUnidad), "Unidad", codigoUnidad);
        String codigoU = codigoUbicacion == null ? UBICACION_POR_DEFECTO : codigoUbicacion;
        UbicacionDespensa ubicacion = Comprobaciones.existe(ubicacionRepository.findByCodigo(codigoU), "Ubicación", codigoU);

        DespensaItem item = new DespensaItem();
        item.setDespensa(despensa);
        item.setIngrediente(ingrediente);
        item.setCantidad(cantidad);
        item.setUnidad(unidad);
        item.setUbicacion(ubicacion);
        if (fechaCaducidad != null) {
            item.setFechaCaducidad(fechaCaducidad);
        } else if (ingrediente.getDiasConservacion() != null) {
            item.setFechaCaducidad(LocalDate.now().plusDays(ingrediente.getDiasConservacion()));
        }
        return itemRepository.save(item);
    }

    /** Saca un producto de la despensa conservando su historial (CONSUMIDO, CADUCADO, DESECHADO, REGALADO u OTRO). */
    @Transactional
    public DespensaItem darDeBaja(Integer idItem, String codigoMotivo) {
        DespensaItem item = Comprobaciones.existe(itemRepository.findById(idItem), "Producto de despensa", idItem);
        if (item.getFechaBaja() != null) {
            throw new ReglaNegocioException("Este producto ya está dado de baja");
        }
        MotivoBaja motivo = Comprobaciones.existe(motivoBajaRepository.findByCodigo(codigoMotivo), "Motivo de baja", codigoMotivo);
        LocalDate hoy = LocalDate.now();
        // La base de datos exige fecha_baja >= fecha_compra
        item.setFechaBaja(hoy.isBefore(item.getFechaCompra()) ? item.getFechaCompra() : hoy);
        item.setMotivoBaja(motivo);
        return itemRepository.save(item);
    }

    public List<DespensaItem> productosDisponibles(Integer idDespensa) {
        return itemRepository.findByDespensaIdAndFechaBajaIsNull(idDespensa);
    }

    /** Productos de todas las despensas del usuario que caducan en los próximos "dias" días (o ya han caducado). */
    public List<DespensaItem> productosQueCaducan(Integer idUsuario, int dias) {
        if (dias < 0) {
            throw new ReglaNegocioException("Los días no pueden ser negativos");
        }
        return itemRepository.buscarQueCaducanAntesDe(idUsuario, LocalDate.now().plusDays(dias));
    }

    /**
     * "¿Qué puedo cocinar hoy?": recetas con todos sus ingredientes obligatorios en la despensa
     * y que además son seguras para el usuario (alergias y dietas).
     */
    public List<Receta> recetasCocinables(Integer idUsuario, Integer idDespensa) {
        Despensa despensa = Comprobaciones.existe(despensaRepository.findById(idDespensa), "Despensa", idDespensa);
        if (!despensa.getUsuario().getId().equals(idUsuario)) {
            throw new ReglaNegocioException("La despensa no pertenece al usuario");
        }
        Set<Integer> seguras = recetaService.recetasSegurasPara(idUsuario).stream()
                .map(Receta::getId)
                .collect(Collectors.toSet());
        return recetaRepository.buscarCocinablesConDespensa(idDespensa).stream()
                .filter(receta -> seguras.contains(receta.getId()))
                .toList();
    }

    /**
     * Devuelve la despensa solo si es del usuario. Si no existe o es de otra persona, el error es el mismo
     * ("no encontrada"), para no revelar que existe.
     */
    public Despensa despensaDelUsuario(Integer idUsuario, Integer idDespensa) {
        Despensa despensa = Comprobaciones.existe(despensaRepository.findById(idDespensa), "Despensa", idDespensa);
        if (!despensa.getUsuario().getId().equals(idUsuario)) {
            throw new RecursoNoEncontradoException("Despensa", idDespensa);
        }
        return despensa;
    }

    /** Devuelve el producto solo si está en esa despensa y la despensa es del usuario. */
    public DespensaItem productoDelUsuario(Integer idUsuario, Integer idDespensa, Integer idItem) {
        Despensa despensa = despensaDelUsuario(idUsuario, idDespensa);
        DespensaItem item = Comprobaciones.existe(itemRepository.findById(idItem), "Producto de despensa", idItem);
        if (!item.getDespensa().getId().equals(despensa.getId())) {
            throw new RecursoNoEncontradoException("Producto de despensa", idItem);
        }
        return item;
    }
}
