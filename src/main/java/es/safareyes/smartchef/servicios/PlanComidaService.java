package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.ResumenDia;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.TipoComida;
import es.safareyes.smartchef.repositorios.IPlanComidaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.calculos.CalculadoraNutricion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanComidaService {

    private final IPlanComidaRepository planRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRecetaRepository recetaRepository;
    private final ITipoComidaRepository tipoComidaRepository;

    /**
     * Planifica una comida. Hace falta una receta o una descripción libre ("Cena fuera").
     *
     * @param codigoTipoComida DESAYUNO, ALMUERZO, COMIDA, MERIENDA o CENA
     * @param horaAviso        si se indica, el usuario recibirá un aviso a esa hora (null = sin aviso)
     */
    @Transactional
    public PlanComida planificar(Integer idUsuario, LocalDate fecha, String codigoTipoComida, Integer idReceta,
                                 String descripcionLibre, int raciones, LocalTime horaAviso) {
        if (fecha == null) {
            throw new ReglaNegocioException("Indica la fecha de la comida");
        }
        if (raciones <= 0) {
            throw new ReglaNegocioException("Las raciones deben ser mayores que 0");
        }
        boolean hayDescripcion = descripcionLibre != null && !descripcionLibre.isBlank();
        if (idReceta == null && !hayDescripcion) {
            throw new ReglaNegocioException("Indica una receta o una descripción");
        }
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        TipoComida tipo = Comprobaciones.existe(tipoComidaRepository.findByCodigo(codigoTipoComida), "Tipo de comida", codigoTipoComida);

        PlanComida plan = new PlanComida();
        plan.setUsuario(usuario);
        plan.setFecha(fecha);
        plan.setTipoComida(tipo);
        if (idReceta != null) {
            plan.setReceta(Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta));
        }
        plan.setDescripcionLibre(hayDescripcion ? descripcionLibre.trim() : null);
        plan.setRaciones((short) raciones);
        plan.setHoraAviso(horaAviso);
        plan.setNotificar(horaAviso != null);
        return planRepository.save(plan);
    }

    /** Las comidas de 7 días a partir de la fecha, ordenadas por día y momento del día. */
    public List<PlanComida> planSemana(Integer idUsuario, LocalDate desde) {
        return planRepository.findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(
                idUsuario, desde, desde.plusDays(6));
    }

    @Transactional
    public PlanComida marcarCompletada(Integer idPlan) {
        PlanComida plan = Comprobaciones.existe(planRepository.findById(idPlan), "Comida planificada", idPlan);
        plan.setCompletada(true);
        return planRepository.save(plan);
    }

    /** Comidas de ese día con aviso activado y sin completar (para las notificaciones push). */
    public List<PlanComida> comidasConAviso(LocalDate fecha) {
        return planRepository.findByFechaAndNotificarTrueAndCompletadaFalse(fecha);
    }

    /** Calorías planificadas para el día frente al objetivo del usuario. */
    public ResumenDia resumenDia(Integer idUsuario, LocalDate fecha) {
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        List<PlanComida> comidas = planRepository
                .findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(idUsuario, fecha, fecha);

        BigDecimal total = BigDecimal.ZERO;
        int sinDatos = 0;
        for (PlanComida comida : comidas) {
            Receta receta = comida.getReceta();
            if (receta == null) {
                sinDatos++;                                     // comida libre: no se conocen sus calorías
                continue;
            }
            ResumenNutricional nutricion = CalculadoraNutricion.paraRaciones(receta, comida.getRaciones());
            total = total.add(nutricion.getCaloriasKcal());
            if (!nutricion.getCompleto()) {
                sinDatos++;
            }
        }

        ResumenDia resumen = new ResumenDia();
        resumen.setFecha(fecha);
        resumen.setTotalComidas(comidas.size());
        resumen.setCaloriasPlanificadas(total);
        resumen.setComidasSinDatos(sinDatos);
        Integer objetivo = usuario.getObjetivoCaloriasDia();
        resumen.setObjetivoCalorias(objetivo);
        resumen.setDiferencia(objetivo == null ? null : BigDecimal.valueOf(objetivo).subtract(total));
        return resumen;
    }

    /** Devuelve la comida planificada solo si es del usuario (si no, "no encontrada"). */
    public PlanComida comidaDelUsuario(Integer idUsuario, Integer idPlan) {
        PlanComida plan = Comprobaciones.existe(planRepository.findById(idPlan), "Comida planificada", idPlan);
        if (!plan.getUsuario().getId().equals(idUsuario)) {
            throw new RecursoNoEncontradoException("Comida planificada", idPlan);
        }
        return plan;
    }
}
