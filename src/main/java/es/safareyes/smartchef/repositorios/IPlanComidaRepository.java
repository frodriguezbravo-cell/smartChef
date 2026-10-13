package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.PlanComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IPlanComidaRepository extends JpaRepository<PlanComida, Integer> {

    /** Plan de un rango de fechas, ordenado por día y por momento del día (desayuno, comida, cena...). */
    List<PlanComida> findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(
            Integer idUsuario, LocalDate desde, LocalDate hasta);

    /** Comidas de un día con aviso activado y sin completar (para las notificaciones push). */
    List<PlanComida> findByFechaAndNotificarTrueAndCompletadaFalse(LocalDate fecha);
}
