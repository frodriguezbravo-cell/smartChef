package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.modelos.catalogos.RolUsuario;
import es.safareyes.smartchef.modelos.catalogos.UnidadTemperatura;
import es.safareyes.smartchef.modelos.catalogos.UnidadVolumen;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellidos", length = 150)
    private String apellidos;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // En BD: DEFAULT 1 (USUARIO). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolUsuario rol;

    // En BD: DEFAULT 1 (C). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_temperatura", nullable = false)
    private UnidadTemperatura unidadTemperatura;

    // En BD: DEFAULT 1 (L). Asígnalo en el servicio antes de guardar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_volumen", nullable = false)
    private UnidadVolumen unidadVolumen;

    @Column(name = "raciones_por_defecto", nullable = false)
    private Short racionesPorDefecto = 2;

    // Opcional: sirve para comparar con las calorías del plan de comidas
    @Column(name = "objetivo_calorias_dia")
    private Integer objetivoCaloriasDia;

    @Column(name = "idioma", nullable = false, length = 5)
    private String idioma = "es";

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = false;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // Dietas que sigue el usuario (tabla intermedia usuario_dieta)
    @ManyToMany
    @JoinTable(name = "usuario_dieta",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_dieta"))
    private List<Dieta> dietas = new ArrayList<>();

    // Alergias e intolerancias (cada una con su gravedad)
    @OneToMany(mappedBy = "usuario")
    private List<UsuarioAlergeno> alergenos = new ArrayList<>();
}
