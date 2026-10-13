package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import jakarta.persistence.*;
import java.math.BigDecimal;
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
@Table(name = "ingrediente")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaIngrediente categoria;

    @Column(name = "descripcion")
    private String descripcion;

    // --- Aptitud para dietas ---
    @Column(name = "apto_vegetariano", nullable = false)
    private Boolean aptoVegetariano = true;

    @Column(name = "apto_vegano", nullable = false)
    private Boolean aptoVegano = true;

    // --- Valores nutricionales POR 100 g (null = dato desconocido) ---
    @Column(name = "calorias_kcal", precision = 6, scale = 1)
    private BigDecimal caloriasKcal;

    @Column(name = "proteinas_g", precision = 5, scale = 2)
    private BigDecimal proteinasG;

    @Column(name = "hidratos_g", precision = 5, scale = 2)
    private BigDecimal hidratosG;

    @Column(name = "azucares_g", precision = 5, scale = 2)
    private BigDecimal azucaresG;

    @Column(name = "grasas_g", precision = 5, scale = 2)
    private BigDecimal grasasG;

    @Column(name = "grasas_saturadas_g", precision = 5, scale = 2)
    private BigDecimal grasasSaturadasG;

    @Column(name = "fibra_g", precision = 5, scale = 2)
    private BigDecimal fibraG;

    @Column(name = "sal_g", precision = 5, scale = 2)
    private BigDecimal salG;

    // --- Temporada (meses 1-12) y conservación ---
    @Column(name = "temporada_inicio")
    private Short temporadaInicio;

    @Column(name = "temporada_fin")
    private Short temporadaFin;

    @Column(name = "dias_conservacion")
    private Short diasConservacion;

    @Column(name = "publicada", nullable = false)
    private Boolean publicada = false;

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

    // Alérgenos que contiene (tabla intermedia ingrediente_alergeno)
    @ManyToMany
    @JoinTable(name = "ingrediente_alergeno",
            joinColumns = @JoinColumn(name = "id_ingrediente"),
            inverseJoinColumns = @JoinColumn(name = "id_alergeno"))
    private List<Alergeno> alergenos = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "ingrediente_origen",
            joinColumns = @JoinColumn(name = "id_ingrediente"),
            inverseJoinColumns = @JoinColumn(name = "id_origen"))
    private List<Origen> origenes = new ArrayList<>();

    @OneToMany(mappedBy = "ingrediente")
    private List<IngredienteImagen> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "ingrediente")
    private List<IngredienteNombreAlternativo> nombresAlternativos = new ArrayList<>();
}
