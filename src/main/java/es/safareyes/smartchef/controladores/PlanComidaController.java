package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.ComidaDto;
import es.safareyes.smartchef.controladores.dto.NuevaComidaPeticion;
import es.safareyes.smartchef.dtos.ResumenDia;
import es.safareyes.smartchef.servicios.PlanComidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Plan de comidas del usuario. Las fechas van en formato ISO: 2026-10-08. */
@RestController
@RequestMapping("/api/usuarios/{idUsuario}/plan-comidas")
@RequiredArgsConstructor
public class PlanComidaController {

    private final PlanComidaService planService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ComidaDto planificar(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaComidaPeticion peticion) {
        return Mapeadores.comida(planService.planificar(idUsuario, peticion.fecha(), peticion.tipoComida(), peticion.idReceta(),
                peticion.descripcion(), peticion.raciones(), peticion.horaAviso()));
    }

    /** GET .../plan-comidas?desde=2026-10-08 : 7 días a partir de esa fecha (sin parámetro, desde hoy). */
    @GetMapping
    @Transactional(readOnly = true)
    public List<ComidaDto> semana(@PathVariable("idUsuario") Integer idUsuario,
                                  @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        LocalDate inicio = desde == null ? LocalDate.now() : desde;
        return planService.planSemana(idUsuario, inicio).stream().map(Mapeadores::comida).toList();
    }

    /** GET .../plan-comidas/resumen?fecha=2026-10-08 : calorías planificadas del día frente al objetivo (sin parámetro, hoy). */
    @GetMapping("/resumen")
    @Transactional(readOnly = true)
    public ResumenDia resumen(@PathVariable("idUsuario") Integer idUsuario,
                              @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return planService.resumenDia(idUsuario, fecha == null ? LocalDate.now() : fecha);
    }

    @PostMapping("/{idPlan}/completar")
    @Transactional
    public ComidaDto completar(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idPlan") Integer idPlan) {
        planService.comidaDelUsuario(idUsuario, idPlan);
        return Mapeadores.comida(planService.marcarCompletada(idPlan));
    }
}
