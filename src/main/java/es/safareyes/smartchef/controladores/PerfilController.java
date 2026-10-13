package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.AlergiaPeticion;
import es.safareyes.smartchef.controladores.dto.DietasPeticion;
import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.servicios.PerfilUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Alergias, intolerancias y dietas del usuario. */
@RestController
@RequestMapping("/api/usuarios/{idUsuario}")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilUsuarioService perfilService;

    @GetMapping("/alergias")
    @Transactional(readOnly = true)
    public List<AlergiaDto> alergias(@PathVariable("idUsuario") Integer idUsuario) {
        return perfilService.alergiasDe(idUsuario).stream().map(Mapeadores::alergia).toList();
    }

    /** PUT /api/usuarios/1/alergias/GLUTEN con {"gravedad":"ALERGIA_GRAVE","notas":"celíaco"}. Si ya la tenía, la actualiza. */
    @PutMapping("/alergias/{codigoAlergeno}")
    @Transactional
    public AlergiaDto anadirAlergia(@PathVariable("idUsuario") Integer idUsuario,
                                    @PathVariable("codigoAlergeno") String codigoAlergeno,
                                    @RequestBody(required = false) @Valid AlergiaPeticion peticion) {
        String gravedad = peticion == null ? null : peticion.gravedad();
        String notas = peticion == null ? null : peticion.notas();
        return Mapeadores.alergia(perfilService.anadirAlergeno(idUsuario, codigoAlergeno, gravedad, notas));
    }

    @DeleteMapping("/alergias/{codigoAlergeno}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void quitarAlergia(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("codigoAlergeno") String codigoAlergeno) {
        perfilService.quitarAlergeno(idUsuario, codigoAlergeno);
    }

    @GetMapping("/dietas")
    @Transactional(readOnly = true)
    public List<OpcionCatalogoDto> dietas(@PathVariable("idUsuario") Integer idUsuario) {
        return Mapeadores.opciones(perfilService.dietasDe(idUsuario));
    }

    /** PUT /api/usuarios/1/dietas con {"dietas":["VEGETARIANA","SIN_GLUTEN"]} : sustituye las dietas del usuario. */
    @PutMapping("/dietas")
    @Transactional
    public List<OpcionCatalogoDto> establecerDietas(@PathVariable("idUsuario") Integer idUsuario,
                                                    @RequestBody @Valid DietasPeticion peticion) {
        return Mapeadores.opciones(perfilService.establecerDietas(idUsuario, peticion.dietas()).getDietas());
    }
}
