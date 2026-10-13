package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.MotivoBaja;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "despensa_item")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DespensaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_despensa", nullable = false)
    private Despensa despensa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @Column(name = "cantidad", nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private UnidadMedida unidad;

    // En BD: DEFAULT 1 (ALACENA). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private UbicacionDespensa ubicacion;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra = LocalDate.now();

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    // Baja: fecha_baja y motivo_baja van juntos. Si fecha_baja es null, el producto sigue disponible.
    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_motivo_baja")
    private MotivoBaja motivoBaja;

    @Column(name = "notas")
    private String notas;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
