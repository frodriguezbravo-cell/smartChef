package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.servicios.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Opciones para los desplegables de la app: dietas, alergenos, gravedades, unidades, tipos-comida, ubicaciones, motivos-baja. */
@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/{nombre}")
    @Transactional(readOnly = true)
    public List<OpcionCatalogoDto> opciones(@PathVariable("nombre") String nombre) {
        return Mapeadores.opciones(catalogoService.opciones(nombre));
    }
}
