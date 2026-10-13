package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.TipoComida;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "plan_comida")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanComida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_comida", nullable = false)
    private TipoComida tipoComida;

    // Debe haber receta o descripción libre (lo exige la BD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @Column(name = "descripcion_libre", length = 150)
    private String descripcionLibre;

    @Column(name = "raciones", nullable = false)
    private Short raciones = 1;

    @Column(name = "hora_aviso")
    private LocalTime horaAviso;

    @Column(name = "notificar", nullable = false)
    private Boolean notificar = false;

    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
