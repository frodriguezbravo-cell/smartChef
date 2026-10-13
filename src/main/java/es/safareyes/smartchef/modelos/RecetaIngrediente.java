package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "receta_ingrediente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecetaIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente;

    // null si la unidad es "al gusto"
    @Column(name = "cantidad", precision = 8, scale = 2)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private UnidadMedida unidad;

    // Equivalencia en gramos: es la base del cálculo nutricional
    @Column(name = "cantidad_gramos", precision = 8, scale = 2)
    private BigDecimal cantidadGramos;

    @Column(name = "grupo", length = 80)
    private String grupo;

    @Column(name = "notas", length = 150)
    private String notas;

    @Column(name = "es_opcional", nullable = false)
    private Boolean esOpcional = false;

    @Column(name = "orden", nullable = false)
    private Short orden = 0;
}
