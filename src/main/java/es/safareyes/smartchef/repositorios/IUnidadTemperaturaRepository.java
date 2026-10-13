package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UnidadTemperatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadTemperaturaRepository extends JpaRepository<UnidadTemperatura, Short> {

    /** Busca una opción por su código estable (por ejemplo "ALACENA"). */
    Optional<UnidadTemperatura> findByCodigo(String codigo);
}
