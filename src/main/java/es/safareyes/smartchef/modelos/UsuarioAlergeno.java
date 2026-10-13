package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usuario_alergeno", uniqueConstraints = {
        @UniqueConstraint(name = "ux_usuario_alergeno", columnNames = {"id_usuario", "id_alergeno"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAlergeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alergeno", nullable = false)
    private Alergeno alergeno;

    // En BD: DEFAULT 2 (ALERGIA_LEVE). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gravedad", nullable = false)
    private Gravedad gravedad;

    @Column(name = "notas", length = 255)
    private String notas;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
