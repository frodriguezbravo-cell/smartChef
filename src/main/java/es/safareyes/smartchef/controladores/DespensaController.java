package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.BajaProductoPeticion;
import es.safareyes.smartchef.controladores.dto.DespensaDto;
import es.safareyes.smartchef.controladores.dto.NuevaDespensaPeticion;
import es.safareyes.smartchef.controladores.dto.NuevoProductoPeticion;
import es.safareyes.smartchef.controladores.dto.ProductoDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.servicios.DespensaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Despensas del usuario. Cada operación sobre una despensa o un producto comprueba primero que sean del usuario
 * de la URL: si no, responde 404 igual que si no existieran.
 */
@RestController
@RequestMapping("/api/usuarios/{idUsuario}")
@RequiredArgsConstructor
public class DespensaController {

    private final DespensaService despensaService;

    @GetMapping("/despensas")
    @Transactional(readOnly = true)
    public List<DespensaDto> despensas(@PathVariable("idUsuario") Integer idUsuario) {
        return despensaService.despensasDe(idUsuario).stream().map(Mapeadores::despensa).toList();
    }

    @PostMapping("/despensas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public DespensaDto crear(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaDespensaPeticion peticion) {
        return Mapeadores.despensa(despensaService.crearDespensa(idUsuario, peticion.nombre()));
    }

    /** Productos que siguen en la despensa (los dados de baja no salen). */
    @GetMapping("/despensas/{idDespensa}/productos")
    @Transactional(readOnly = true)
    public List<ProductoDto> productos(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa) {
        despensaService.despensaDelUsuario(idUsuario, idDespensa);
        return despensaService.productosDisponibles(idDespensa).stream().map(Mapeadores::producto).toList();
    }

    @PostMapping("/despensas/{idDespensa}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ProductoDto anadirProducto(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa,
                                      @RequestBody @Valid NuevoProductoPeticion peticion) {
        despensaService.despensaDelUsuario(idUsuario, idDespensa);
        return Mapeadores.producto(despensaService.anadirProducto(idDespensa, peticion.idIngrediente(), peticion.cantidad(),
                peticion.unidad(), peticion.ubicacion(), peticion.fechaCaducidad()));
    }

    /** POST .../productos/5/baja con {"motivo":"CONSUMIDO"} : saca el producto de la despensa conservando el historial. */
    @PostMapping("/despensas/{idDespensa}/productos/{idProducto}/baja")
    @Transactional
    public ProductoDto darDeBaja(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa,
                                 @PathVariable("idProducto") Integer idProducto, @RequestBody @Valid BajaProductoPeticion peticion) {
        despensaService.productoDelUsuario(idUsuario, idDespensa, idProducto);
        return Mapeadores.producto(despensaService.darDeBaja(idProducto, peticion.motivo()));
    }

    /** GET .../despensas/5/cocinables : "¿qué puedo cocinar hoy?" (con lo que hay y sin sus alérgenos ni dietas prohibidas). */
    @GetMapping("/despensas/{idDespensa}/cocinables")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> cocinables(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa) {
        return despensaService.recetasCocinables(idUsuario, idDespensa).stream().map(Mapeadores::resumen).toList();
    }

    /** GET /api/usuarios/1/productos/caducan?dias=3 : productos de todas sus despensas que caducan en 3 días o ya han caducado. */
    @GetMapping("/productos/caducan")
    @Transactional(readOnly = true)
    public List<ProductoDto> caducan(@PathVariable("idUsuario") Integer idUsuario,
                                     @RequestParam(name = "dias", defaultValue = "3") int dias) {
        return despensaService.productosQueCaducan(idUsuario, dias).stream().map(Mapeadores::producto).toList();
    }
}
