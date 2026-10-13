package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IIngredienteRepository extends JpaRepository<Ingrediente, Integer> {

    Optional<Ingrediente> findBySlug(String slug);

    List<Ingrediente> findByNombreContainingIgnoreCase(String texto);

    List<Ingrediente> findByCategoriaIdAndPublicadaTrue(Integer idCategoria);

    List<Ingrediente> findByAptoVeganoTrueAndPublicadaTrue();

    /**
     * JPQL: ingredientes publicados que NO contienen ninguno de los alérgenos indicados.
     * La lista no puede estar vacía: si el usuario no tiene alergias, el servicio
     * debe llamar a findAll() en lugar de a este método.
     *
     * @param codigos códigos de alérgeno, por ejemplo List.of("GLUTEN", "LACTEOS")
     */
    @Query("select i from Ingrediente i where i.publicada = true " +
            "and not exists (select a.id from i.alergenos a where a.codigo in :codigos)")
    List<Ingrediente> buscarSinAlergenos(@Param("codigos") List<String> codigos);
}
