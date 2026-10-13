package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.modelos.catalogos.Dificultad;
import es.safareyes.smartchef.modelos.catalogos.MetodoCoccion;
import es.safareyes.smartchef.modelos.catalogos.TipoCocina;
import es.safareyes.smartchef.modelos.catalogos.TipoPlato;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "receta")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "slug", nullable = false, unique = true, length = 220)
    private String slug;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_plato", nullable = false)
    private TipoPlato tipoPlato;

    // En BD: DEFAULT 7 (INTERNACIONAL). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_cocina", nullable = false)
    private TipoCocina tipoCocina;

    // En BD: DEFAULT 1 (FACIL). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dificultad", nullable = false)
    private Dificultad dificultad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_coccion")
    private MetodoCoccion metodoCoccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origen")
    private Origen origen;

    @Column(name = "raciones", nullable = false)
    private Short raciones = 2;

    @Column(name = "tiempo_preparacion_min")
    private Short tiempoPreparacionMin;

    @Column(name = "tiempo_coccion_min")
    private Short tiempoCoccionMin;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "publicada", nullable = false)
    private Boolean publicada = false;

    @Column(name = "es_publica", nullable = false)
    private Boolean esPublica = false;

    @Column(name = "fuentes")
    private String fuentes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // Ingredientes y pasos forman parte de la receta: se guardan y se borran con ella.
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<RecetaIngrediente> ingredientes = new ArrayList<>();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<RecetaPaso> pasos = new ArrayList<>();

    @OneToMany(mappedBy = "receta")
    @OrderBy("orden ASC")
    private List<RecetaImagen> imagenes = new ArrayList<>();

    // Dietas para las que es apta (tabla intermedia receta_dieta)
    @ManyToMany
    @JoinTable(name = "receta_dieta",
            joinColumns = @JoinColumn(name = "id_receta"),
            inverseJoinColumns = @JoinColumn(name = "id_dieta"))
    private List<Dieta> dietas = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "receta_utensilio",
            joinColumns = @JoinColumn(name = "id_receta"),
            inverseJoinColumns = @JoinColumn(name = "id_utensilio"))
    private List<Utensilio> utensilios = new ArrayList<>();
}
