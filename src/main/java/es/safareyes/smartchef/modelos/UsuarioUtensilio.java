package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_utensilio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUtensilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Debe haber utensilio del catálogo o descripción libre (lo exige la BD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utensilio")
    private Utensilio utensilio;

    @Column(name = "descripcion_libre", length = 150)
    private String descripcionLibre;

    @Column(name = "cantidad", nullable = false)
    private Short cantidad = 1;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "notas")
    private String notas;
}
