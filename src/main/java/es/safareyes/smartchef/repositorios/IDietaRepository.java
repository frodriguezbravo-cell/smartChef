package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Dieta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IDietaRepository extends JpaRepository<Dieta, Short> {

    /** Busca una opción del catálogo por su código estable (FACIL, GLUTEN, ALACENA...). */
    Optional<Dieta> findByCodigo(String codigo);
}
