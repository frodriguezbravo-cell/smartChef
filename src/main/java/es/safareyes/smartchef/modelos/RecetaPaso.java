package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "receta_paso", uniqueConstraints = {
        @UniqueConstraint(name = "ux_receta_paso", columnNames = {"id_receta", "numero"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecetaPaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @Column(name = "numero", nullable = false)
    private Short numero;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "duracion_min")
    private Short duracionMin;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
}
