package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface IUsuarioAlergenoRepository extends JpaRepository<UsuarioAlergeno, Integer> {

    List<UsuarioAlergeno> findByUsuarioId(Integer idUsuario);

    Optional<UsuarioAlergeno> findByUsuarioIdAndAlergenoId(Integer idUsuario, Short idAlergeno);

    boolean existsByUsuarioIdAndAlergenoId(Integer idUsuario, Short idAlergeno);

    /**
     * JPQL: códigos de los alérgenos del usuario (GLUTEN, LACTEOS...).
     * Es justo lo que esperan buscarSinAlergenos(...) de recetas e ingredientes.
     */
    @Query("select ua.alergeno.codigo from UsuarioAlergeno ua where ua.usuario.id = :idUsuario")
    List<String> codigosDeAlergenos(@Param("idUsuario") Integer idUsuario);
}
