package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.NutricionReceta;
import es.safareyes.smartchef.dtos.RecetasPorTipoPlato;
import es.safareyes.smartchef.modelos.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRecetaRepository extends JpaRepository<Receta, Integer> {

    Optional<Receta> findBySlug(String slug);

    List<Receta> findByPublicadaTrue();

    List<Receta> findByTituloContainingIgnoreCaseAndPublicadaTrue(String texto);

    /** Usando JPA Interfaz con una relación: dificultad.codigo (FACIL, MEDIA...). */
    List<Receta> findByDificultadCodigoAndPublicadaTrue(String codigoDificultad);

    /** JPQL: recetas cuyo tiempo total (preparación + cocción) no supera los minutos indicados. */
    @Query("select r from Receta r where r.publicada = true " +
            "and (coalesce(r.tiempoPreparacionMin, 0) + coalesce(r.tiempoCoccionMin, 0)) <= :minutos")
    List<Receta> buscarRapidas(@Param("minutos") Integer minutos);

    /** JPQL: recetas aptas para una dieta (VEGANA, SIN_GLUTEN...). */
    @Query("select distinct r from Receta r join r.dietas d " +
            "where r.publicada = true and d.codigo = :codigoDieta")
    List<Receta> buscarPorDieta(@Param("codigoDieta") String codigoDieta);

    /**
     * JPQL: recetas publicadas sin ningún ingrediente con esos alérgenos.
     * Se incluyen los ingredientes opcionales, por seguridad.
     * La lista no puede estar vacía (ver Ingrediente).
     */
    @Query("select r from Receta r where r.publicada = true and not exists (" +
            "select ri.id from RecetaIngrediente ri join ri.ingrediente i join i.alergenos a " +
            "where ri.receta = r and a.codigo in :codigos)")
    List<Receta> buscarSinAlergenos(@Param("codigos") List<String> codigos);

    /**
     * JPQL con subconsultas anidadas: "¿qué puedo cocinar hoy?".
     * Recetas en las que NO falta ningún ingrediente obligatorio en la despensa indicada
     * (solo cuentan los productos que no se han dado de baja).
     */
    @Query("select r from Receta r where r.publicada = true and not exists (" +
            "select ri.id from RecetaIngrediente ri where ri.receta = r and ri.esOpcional = false " +
            "and not exists (select di.id from DespensaItem di where di.despensa.id = :idDespensa " +
            "and di.fechaBaja is null and di.ingrediente = ri.ingrediente))")
    List<Receta> buscarCocinablesConDespensa(@Param("idDespensa") Integer idDespensa);

    /** JPQL con constructor de DTO: número de recetas publicadas de cada tipo de plato. */
    @Query("select new es.safareyes.smartchef.dtos.RecetasPorTipoPlato(tp.nombre, count(r)) " +
            "from Receta r join r.tipoPlato tp where r.publicada = true " +
            "group by tp.nombre, tp.orden order by tp.orden")
    List<RecetasPorTipoPlato> contarRecetasPorTipoPlato();

    /**
     * SQL nativo sobre la vista v_receta_nutricion (nutrición POR RACIÓN).
     * Los alias van entre comillas para que coincidan con los getters del interface.
     * Devuelve vacío si la receta no tiene ingredientes con cantidad_gramos.
     */
    @Query(nativeQuery = true, value = "select id_receta as \"idReceta\", titulo as \"titulo\", " +
            "raciones as \"raciones\", calorias_kcal as \"caloriasKcal\", proteinas_g as \"proteinasG\", " +
            "hidratos_g as \"hidratosG\", azucares_g as \"azucaresG\", grasas_g as \"grasasG\", " +
            "grasas_saturadas_g as \"grasasSaturadasG\", fibra_g as \"fibraG\", sal_g as \"salG\" " +
            "from v_receta_nutricion where id_receta = :idReceta")
    Optional<NutricionReceta> nutricionPorRacion(@Param("idReceta") Integer idReceta);
}
