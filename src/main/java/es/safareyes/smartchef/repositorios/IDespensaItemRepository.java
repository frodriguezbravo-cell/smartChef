package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DespensaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IDespensaItemRepository extends JpaRepository<DespensaItem, Integer> {

    /** Productos que siguen en la despensa (sin fecha de baja). */
    List<DespensaItem> findByDespensaIdAndFechaBajaIsNull(Integer idDespensa);

    /**
     * JPQL: productos de un usuario que caducan antes de la fecha límite.
     * Sirve para los avisos push de caducidad.
     */
    @Query("select di from DespensaItem di where di.despensa.usuario.id = :idUsuario " +
            "and di.fechaBaja is null and di.fechaCaducidad is not null " +
            "and di.fechaCaducidad <= :limite order by di.fechaCaducidad")
    List<DespensaItem> buscarQueCaducanAntesDe(@Param("idUsuario") Integer idUsuario,
                                              @Param("limite") LocalDate limite);
}
