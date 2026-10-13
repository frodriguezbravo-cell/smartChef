package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Continente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IContinenteRepository extends JpaRepository<Continente, Short> {

    /**
     * Usando JPA Interfaz: Spring construye la consulta a partir del nombre del método.
     * OJO: el id y el orden de los catálogos son Short, por eso el parámetro es Short.
     */
    List<Continente> findAllByNombreContainingAndOrdenIsLessThan(String nombre, Short orden);
}
