package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IMarcaRepository extends JpaRepository<Marca, Integer> {

    Optional<Marca> findByNombre(String nombre);
}
