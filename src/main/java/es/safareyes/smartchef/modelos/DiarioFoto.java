package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diario_foto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiarioFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diario", nullable = false)
    private DiarioCocina diario;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tomada_en", nullable = false)
    private LocalDateTime tomadaEn = LocalDateTime.now();
}
