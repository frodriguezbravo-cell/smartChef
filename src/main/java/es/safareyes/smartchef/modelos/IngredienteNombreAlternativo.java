package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingrediente_nombre_alternativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteNombreAlternativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "idioma", nullable = false, length = 5)
    private String idioma = "es";
}
