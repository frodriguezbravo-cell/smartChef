package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AnadirRecetaPeticion;
import es.safareyes.smartchef.controladores.dto.CompradoPeticion;
import es.safareyes.smartchef.controladores.dto.LineaListaDto;
import es.safareyes.smartchef.controladores.dto.ListaDetalleDto;
import es.safareyes.smartchef.controladores.dto.ListaDto;
import es.safareyes.smartchef.controladores.dto.NuevaListaPeticion;
import es.safareyes.smartchef.controladores.dto.TrasladoDto;
import es.safareyes.smartchef.controladores.dto.TrasladoPeticion;
import es.safareyes.smartchef.servicios.ListaCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Listas de la compra del usuario. Solo se puede operar sobre listas y líneas propias (si no, 404). */
@RestController
@RequestMapping("/api/usuarios/{idUsuario}/listas-compra")
@RequiredArgsConstructor
public class ListaCompraController {

    private final ListaCompraService listaService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<ListaDto> listas(@PathVariable("idUsuario") Integer idUsuario) {
        return listaService.listasDe(idUsuario).stream().map(Mapeadores::lista).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ListaDto crear(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaListaPeticion peticion) {
        return Mapeadores.lista(listaService.crearLista(idUsuario, peticion.nombre()));
    }

    @GetMapping("/{idLista}")
    @Transactional(readOnly = true)
    public ListaDetalleDto detalle(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista) {
        return Mapeadores.listaDetalle(listaService.listaDelUsuario(idUsuario, idLista));
    }

    /** POST .../listas-compra/3/recetas con {"idReceta":7,"raciones":4,"idDespensa":5} : añade lo que falta para cocinarla. */
    @PostMapping("/{idLista}/recetas")
    @Transactional
    public ListaDetalleDto anadirReceta(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                        @RequestBody @Valid AnadirRecetaPeticion peticion) {
        listaService.listaDelUsuario(idUsuario, idLista);
        return Mapeadores.listaDetalle(listaService.anadirReceta(idLista, peticion.idReceta(), peticion.raciones(), peticion.idDespensa()));
    }

    /** PATCH .../listas-compra/3/lineas/9 con {"comprado":true}. */
    @PatchMapping("/{idLista}/lineas/{idLinea}")
    @Transactional
    public LineaListaDto marcarComprado(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                        @PathVariable("idLinea") Integer idLinea, @RequestBody @Valid CompradoPeticion peticion) {
        listaService.lineaDeLista(idUsuario, idLista, idLinea);
        return Mapeadores.linea(listaService.marcarComprado(idLinea, peticion.comprado()));
    }

    /** POST .../listas-compra/3/trasladar-comprados con {"idDespensa":5} : pasa lo comprado a la despensa y lo quita de la lista. */
    @PostMapping("/{idLista}/trasladar-comprados")
    @Transactional
    public TrasladoDto trasladarComprados(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                          @RequestBody @Valid TrasladoPeticion peticion) {
        listaService.listaDelUsuario(idUsuario, idLista);
        return new TrasladoDto(listaService.pasarCompradosADespensa(idLista, peticion.idDespensa()));
    }
}
