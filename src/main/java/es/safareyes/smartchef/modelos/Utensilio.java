package es.safareyes.smartchef.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "utensilio")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utensilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaUtensilio categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @Column(name = "modelo", nullable = false, length = 150)
    private String modelo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "potencia_w", precision = 6, scale = 1)
    private BigDecimal potenciaW;

    @Column(name = "capacidad_l", precision = 5, scale = 2)
    private BigDecimal capacidadL;

    @Column(name = "especificaciones")
    private String especificaciones;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
