package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.RecetaDetalleDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.servicios.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;

    /** GET /api/recetas?titulo=pasta : recetas publicadas cuyo título contiene el texto. */
    @GetMapping("/recetas")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> buscar(@RequestParam("titulo") String titulo) {
        return recetaService.buscarPorTitulo(titulo).stream().map(Mapeadores::resumen).toList();
    }

    /** GET /api/recetas/rapidas?minutos=30 : recetas cuyo tiempo total no supera los minutos indicados. */
    @GetMapping("/recetas/rapidas")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> rapidas(@RequestParam(name = "minutos", defaultValue = "30") int minutos) {
        return recetaService.recetasRapidas(minutos).stream().map(Mapeadores::resumen).toList();
    }

    /**
     * GET /api/recetas/7?raciones=4 : receta con ingredientes y nutrición para ese número de raciones
     * (sin el parámetro, las raciones de la receta). La nutrición es el TOTAL de las raciones pedidas.
     */
    @GetMapping("/recetas/{idReceta}")
    @Transactional(readOnly = true)
    public RecetaDetalleDto detalle(@PathVariable("idReceta") Integer idReceta,
                                    @RequestParam(name = "raciones", required = false) Integer raciones) {
        Receta receta = recetaService.obtenerPorId(idReceta);
        int solicitadas = raciones == null ? receta.getRaciones() : raciones;
        List<IngredienteEscalado> ingredientes = recetaService.ingredientesPara(idReceta, solicitadas);
        ResumenNutricional nutricion = recetaService.nutricion(idReceta, solicitadas);
        return Mapeadores.detalle(receta, solicitadas, ingredientes, nutricion);
    }

    /** GET /api/usuarios/1/recetas-seguras : recetas sin sus alérgenos y aptas para todas sus dietas. */
    @GetMapping("/usuarios/{idUsuario}/recetas-seguras")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> seguras(@PathVariable("idUsuario") Integer idUsuario) {
        return recetaService.recetasSegurasPara(idUsuario).stream().map(Mapeadores::resumen).toList();
    }

    /** GET /api/usuarios/1/recetas/7/avisos : alergias del usuario que afectan a esa receta (vacío = segura). */
    @GetMapping("/usuarios/{idUsuario}/recetas/{idReceta}/avisos")
    @Transactional(readOnly = true)
    public List<AlergiaDto> avisos(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idReceta") Integer idReceta) {
        return recetaService.avisosDeAlergenos(idUsuario, idReceta).stream().map(Mapeadores::alergia).toList();
    }
}
