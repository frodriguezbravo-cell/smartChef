package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IListaCompraItemRepository;
import es.safareyes.smartchef.repositorios.IListaCompraRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.calculos.CalculadoraCompra;
import es.safareyes.smartchef.servicios.calculos.Escalador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListaCompraService {

    private static final String UBICACION_POR_DEFECTO = "ALACENA";

    private final IListaCompraRepository listaRepository;
    private final IListaCompraItemRepository itemRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRecetaRepository recetaRepository;
    private final IDespensaRepository despensaRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final IUbicacionDespensaRepository ubicacionRepository;

    public List<ListaCompra> listasDe(Integer idUsuario) {
        return listaRepository.findByUsuarioIdOrderByCreadoEnDesc(idUsuario);
    }

    @Transactional
    public ListaCompra crearLista(Integer idUsuario, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("La lista necesita un nombre");
        }
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        ListaCompra lista = new ListaCompra();
        lista.setUsuario(usuario);
        lista.setNombre(nombre.trim());
        return listaRepository.save(lista);
    }

    /**
     * Añade a la lista lo que falta para cocinar la receta con las raciones indicadas.
     * Si se indica despensa, solo se añade lo que NO hay en ella. Si la lista ya tenía ese ingrediente
     * pendiente de comprar, se suma la cantidad en lugar de duplicar la línea.
     *
     * @param idDespensa despensa con la que comparar (null = añadir todos los ingredientes obligatorios)
     */
    @Transactional
    public ListaCompra anadirReceta(Integer idLista, Integer idReceta, int raciones, Integer idDespensa) {
        if (raciones <= 0) {
            throw new ReglaNegocioException("Las raciones deben ser mayores que 0");
        }
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);

        List<DespensaItem> enDespensa = List.of();
        if (idDespensa != null) {
            Despensa despensa = despensaDelMismoUsuario(idDespensa, lista);
            enDespensa = despensaItemRepository.findByDespensaIdAndFechaBajaIsNull(despensa.getId());
        }

        BigDecimal factor = Escalador.factor(receta, raciones);
        for (CalculadoraCompra.Faltante faltante : CalculadoraCompra.faltantes(receta.getIngredientes(), factor, enDespensa)) {
            fusionar(lista, faltante, receta);
        }
        return listaRepository.save(lista);
    }

    @Transactional
    public ListaCompraItem marcarComprado(Integer idItem, boolean comprado) {
        ListaCompraItem item = Comprobaciones.existe(itemRepository.findById(idItem), "Línea de la lista", idItem);
        item.setComprado(comprado);
        return itemRepository.save(item);
    }

    /**
     * Pasa a la despensa lo que ya está comprado y lo quita de la lista.
     * Solo se trasladan las líneas con ingrediente, cantidad y unidad (las "al gusto" no).
     * Si no queda nada pendiente, la lista se marca como completada.
     *
     * @return número de líneas trasladadas
     */
    @Transactional
    public int pasarCompradosADespensa(Integer idLista, Integer idDespensa) {
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        Despensa despensa = despensaDelMismoUsuario(idDespensa, lista);
        UbicacionDespensa ubicacion = Comprobaciones.existe(
                ubicacionRepository.findByCodigo(UBICACION_POR_DEFECTO), "Ubicación", UBICACION_POR_DEFECTO);

        List<ListaCompraItem> trasladados = new ArrayList<>();
        for (ListaCompraItem linea : lista.getItems()) {
            boolean trasladable = Boolean.TRUE.equals(linea.getComprado()) && linea.getIngrediente() != null
                    && linea.getCantidad() != null && linea.getUnidad() != null;
            if (!trasladable) {
                continue;
            }
            DespensaItem producto = new DespensaItem();
            producto.setDespensa(despensa);
            producto.setIngrediente(linea.getIngrediente());
            producto.setCantidad(linea.getCantidad());
            producto.setUnidad(linea.getUnidad());
            producto.setUbicacion(ubicacion);
            if (linea.getIngrediente().getDiasConservacion() != null) {
                producto.setFechaCaducidad(LocalDate.now().plusDays(linea.getIngrediente().getDiasConservacion()));
            }
            despensaItemRepository.save(producto);
            trasladados.add(linea);
        }

        if (!trasladados.isEmpty()) {
            lista.getItems().removeAll(trasladados);            // orphanRemoval las borra de la base de datos
            boolean quedanPendientes = lista.getItems().stream()
                    .anyMatch(linea -> !Boolean.TRUE.equals(linea.getComprado()));
            lista.setCompletada(!quedanPendientes);
            listaRepository.save(lista);
        }
        return trasladados.size();
    }

    private Despensa despensaDelMismoUsuario(Integer idDespensa, ListaCompra lista) {
        Despensa despensa = Comprobaciones.existe(despensaRepository.findById(idDespensa), "Despensa", idDespensa);
        if (!despensa.getUsuario().getId().equals(lista.getUsuario().getId())) {
            throw new ReglaNegocioException("La despensa no pertenece al dueño de la lista");
        }
        return despensa;
    }

    /** Suma a una línea pendiente del mismo ingrediente y unidad, o crea una línea nueva. */
    private void fusionar(ListaCompra lista, CalculadoraCompra.Faltante faltante, Receta receta) {
        for (ListaCompraItem existente : lista.getItems()) {
            boolean mismaLinea = !Boolean.TRUE.equals(existente.getComprado())
                    && existente.getIngrediente() != null
                    && Objects.equals(existente.getIngrediente().getId(), faltante.ingrediente().getId())
                    && Objects.equals(idUnidad(existente), faltante.unidad() == null ? null : faltante.unidad().getId());
            if (mismaLinea) {
                if (faltante.cantidad() != null) {
                    existente.setCantidad(existente.getCantidad() == null
                            ? faltante.cantidad()
                            : existente.getCantidad().add(faltante.cantidad()));
                }
                return;
            }
        }
        ListaCompraItem nueva = new ListaCompraItem();
        nueva.setLista(lista);
        nueva.setIngrediente(faltante.ingrediente());
        nueva.setCantidad(faltante.cantidad());
        nueva.setUnidad(faltante.unidad());
        nueva.setReceta(receta);
        lista.getItems().add(nueva);
    }

    private static Short idUnidad(ListaCompraItem linea) {
        return linea.getUnidad() == null ? null : linea.getUnidad().getId();
    }

    /** Devuelve la lista solo si es del usuario (si no, "no encontrada"). */
    public ListaCompra listaDelUsuario(Integer idUsuario, Integer idLista) {
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        if (!lista.getUsuario().getId().equals(idUsuario)) {
            throw new RecursoNoEncontradoException("Lista de la compra", idLista);
        }
        return lista;
    }

    /** Devuelve la línea solo si está en esa lista y la lista es del usuario. */
    public ListaCompraItem lineaDeLista(Integer idUsuario, Integer idLista, Integer idLinea) {
        ListaCompra lista = listaDelUsuario(idUsuario, idLista);
        ListaCompraItem linea = Comprobaciones.existe(itemRepository.findById(idLinea), "Línea de la lista", idLinea);
        if (!linea.getLista().getId().equals(lista.getId())) {
            throw new RecursoNoEncontradoException("Línea de la lista", idLinea);
        }
        return linea;
    }
}
