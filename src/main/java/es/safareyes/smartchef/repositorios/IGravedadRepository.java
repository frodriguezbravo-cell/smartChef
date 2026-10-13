package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IGravedadRepository extends JpaRepository<Gravedad, Short> {

    /** Busca una opción del catálogo por su código estable (FACIL, GLUTEN, ALACENA...). */
    Optional<Gravedad> findByCodigo(String codigo);
}
