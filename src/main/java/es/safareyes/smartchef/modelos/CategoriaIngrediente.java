package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_ingrediente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "orden", nullable = false)
    private Short orden = 0;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Ingrediente> ingredientes = new ArrayList<>();
}
