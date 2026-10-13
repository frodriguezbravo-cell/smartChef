package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IUbicacionDespensaRepository extends JpaRepository<UbicacionDespensa, Short> {

    /** Busca una opción del catálogo por su código estable (FACIL, GLUTEN, ALACENA...). */
    Optional<UbicacionDespensa> findByCodigo(String codigo);
}
