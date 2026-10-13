package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IUnidadMedidaRepository extends JpaRepository<UnidadMedida, Short> {

    /** Busca una opción del catálogo por su código estable (FACIL, GLUTEN, ALACENA...). */
    Optional<UnidadMedida> findByCodigo(String codigo);
}
