package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.Continente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "origen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Origen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_continente", nullable = false)
    private Continente continente;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
}
