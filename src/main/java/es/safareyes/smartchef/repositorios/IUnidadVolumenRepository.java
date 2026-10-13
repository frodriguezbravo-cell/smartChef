package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UnidadVolumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadVolumenRepository extends JpaRepository<UnidadVolumen, Short> {

    /** Busca una opción por su código estable (por ejemplo "ALACENA"). */
    Optional<UnidadVolumen> findByCodigo(String codigo);
}
