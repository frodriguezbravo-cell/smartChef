package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lista_compra_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListaCompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lista", nullable = false)
    private ListaCompra lista;

    // Debe haber ingrediente o descripción libre (lo exige la BD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingrediente")
    private Ingrediente ingrediente;

    @Column(name = "descripcion_libre", length = 150)
    private String descripcionLibre;

    @Column(name = "cantidad", precision = 8, scale = 2)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad")
    private UnidadMedida unidad;

    // Receta que originó esta línea (si se añadió desde una receta)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @Column(name = "comprado", nullable = false)
    private Boolean comprado = false;
}
