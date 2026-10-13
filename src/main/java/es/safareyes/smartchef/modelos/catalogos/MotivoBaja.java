package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=CONSUMIDO, 2=CADUCADO, 3=DESECHADO, 4=REGALADO, 5=OTRO */
@Entity
@Table(name = "cat_motivo_baja")
@NoArgsConstructor
public class MotivoBaja extends Catalogo {
}
