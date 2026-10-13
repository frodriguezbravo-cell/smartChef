package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.ComidaDto;
import es.safareyes.smartchef.controladores.dto.DespensaDto;
import es.safareyes.smartchef.controladores.dto.LineaListaDto;
import es.safareyes.smartchef.controladores.dto.ListaDetalleDto;
import es.safareyes.smartchef.controladores.dto.ListaDto;
import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.controladores.dto.PasoDto;
import es.safareyes.smartchef.controladores.dto.ProductoDto;
import es.safareyes.smartchef.controladores.dto.RecetaDetalleDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Convierte entidades en las respuestas de la API.
 * IMPORTANTE: las entidades tienen relaciones perezosas (LAZY) y open-in-view está desactivado,
 * así que estos métodos solo pueden llamarse dentro de una transacción
 * (los métodos de los controladores llevan @Transactional).
 */
final class Mapeadores {

    private Mapeadores() {
    }

    static OpcionCatalogoDto opcion(Catalogo c) {
        return new OpcionCatalogoDto(c.getCodigo(), c.getNombre(), c.getDescripcion());
    }

    static List<OpcionCatalogoDto> opciones(List<? extends Catalogo> catalogos) {
        return catalogos.stream().map(Mapeadores::opcion).toList();
    }

    private static String nombre(Catalogo c) {
        return c == null ? null : c.getNombre();
    }

    // ---------------------------------------------------------------- recetas
    static RecetaResumenDto resumen(Receta r) {
        int total = (r.getTiempoPreparacionMin() == null ? 0 : r.getTiempoPreparacionMin())
                + (r.getTiempoCoccionMin() == null ? 0 : r.getTiempoCoccionMin());
        return new RecetaResumenDto(r.getId(), r.getTitulo(), r.getSlug(), r.getImagenUrl(), nombre(r.getTipoPlato()),
                nombre(r.getTipoCocina()), nombre(r.getDificultad()), total, r.getRaciones(),
                r.getDietas().stream().map(Catalogo::getCodigo).toList());
    }

    static RecetaDetalleDto detalle(Receta r, int racionesSolicitadas, List<IngredienteEscalado> ingredientes,
                                    ResumenNutricional nutricion) {
        List<String> alergenos = r.getIngredientes().stream()
                .flatMap(linea -> linea.getIngrediente().getAlergenos().stream())
                .map(Catalogo::getNombre)
                .distinct()
                .toList();
        List<PasoDto> pasos = r.getPasos().stream()
                .map(p -> new PasoDto(p.getNumero(), p.getDescripcion(), p.getDuracionMin()))
                .toList();
        return new RecetaDetalleDto(r.getId(), r.getTitulo(), r.getSlug(), r.getDescripcion(), r.getImagenUrl(),
                nombre(r.getTipoPlato()), nombre(r.getTipoCocina()), nombre(r.getDificultad()),
                r.getTiempoPreparacionMin(), r.getTiempoCoccionMin(), r.getRaciones(), racionesSolicitadas,
                r.getDietas().stream().map(Catalogo::getCodigo).toList(), alergenos, ingredientes, pasos, nutricion);
    }

    static AlergiaDto alergia(UsuarioAlergeno a) {
        return new AlergiaDto(a.getAlergeno().getCodigo(), a.getAlergeno().getNombre(), nombre(a.getGravedad()), a.getNotas());
    }

    // ---------------------------------------------------------------- despensa
    static DespensaDto despensa(Despensa d) {
        return new DespensaDto(d.getId(), d.getNombre(), d.getNotas());
    }

    static ProductoDto producto(DespensaItem i) {
        Long dias = i.getFechaCaducidad() == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), i.getFechaCaducidad());
        return new ProductoDto(i.getId(), i.getIngrediente().getId(), i.getIngrediente().getNombre(), i.getCantidad(),
                nombre(i.getUnidad()), nombre(i.getUbicacion()), i.getFechaCompra(), i.getFechaCaducidad(), dias,
                i.getFechaBaja(), nombre(i.getMotivoBaja()));
    }

    // ---------------------------------------------------------------- lista de la compra
    static ListaDto lista(ListaCompra l) {
        int pendientes = (int) l.getItems().stream().filter(linea -> !Boolean.TRUE.equals(linea.getComprado())).count();
        return new ListaDto(l.getId(), l.getNombre(), l.getCompletada(), l.getItems().size(), pendientes);
    }

    static LineaListaDto linea(ListaCompraItem l) {
        String descripcion = l.getIngrediente() != null ? l.getIngrediente().getNombre() : l.getDescripcionLibre();
        return new LineaListaDto(l.getId(), l.getIngrediente() == null ? null : l.getIngrediente().getId(), descripcion,
                l.getCantidad(), nombre(l.getUnidad()), l.getReceta() == null ? null : l.getReceta().getTitulo(), l.getComprado());
    }

    static ListaDetalleDto listaDetalle(ListaCompra l) {
        return new ListaDetalleDto(l.getId(), l.getNombre(), l.getCompletada(),
                l.getItems().stream().map(Mapeadores::linea).toList());
    }

    // ---------------------------------------------------------------- plan de comidas
    static ComidaDto comida(PlanComida p) {
        String descripcion = p.getReceta() != null ? p.getReceta().getTitulo() : p.getDescripcionLibre();
        return new ComidaDto(p.getId(), p.getFecha(), nombre(p.getTipoComida()), p.getReceta() == null ? null : p.getReceta().getId(),
                descripcion, p.getRaciones(), p.getHoraAviso(), p.getNotificar(), p.getCompletada());
    }
}
