package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_utensilio", uniqueConstraints = {
        @UniqueConstraint(name = "ux_categoria_utensilio_nombre_padre", columnNames = {"nombre", "id_categoria_padre"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaUtensilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private CategoriaUtensilio categoriaPadre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "orden", nullable = false)
    private Short orden = 0;

    @OneToMany(mappedBy = "categoriaPadre")
    private List<CategoriaUtensilio> subcategorias = new ArrayList<>();
}
