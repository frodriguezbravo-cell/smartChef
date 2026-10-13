-- =====================================================================
--  SMARTCHEF · Modelo de datos PostgreSQL (versión didáctica)
--  Recetas + despensa + plan de comidas + lista de la compra,
--  con dietas, alérgenos y valores nutricionales.
--  API REST con Spring Boot (Spring Data JPA) + aplicación móvil
--  Compatible con PostgreSQL 12 o superior
-- =====================================================================
--  Convenciones:
--   · Clave primaria:  id SERIAL          (catálogos: id SMALLSERIAL)
--   · Clave foránea:   id_<tabla>         (id_usuario, id_ingrediente, id_dieta...)
--   · Opciones:        tabla cat_xxx + columna id_xxx SMALLINT
--   · Tipos en Java:   SERIAL -> Integer · SMALLINT -> Short · NUMERIC -> BigDecimal
--                      DATE -> LocalDate · TIMESTAMP -> LocalDateTime · TIME -> LocalTime
--   · creado_en / actualizado_en -> @CreatedDate / @LastModifiedDate en la entidad
--   · Imágenes: se guarda la URL, no el archivo
--   · Valores nutricionales: SIEMPRE por 100 g de ingrediente
--   · La lógica (alertas, cálculos, avisos) se programa en los servicios Java
-- =====================================================================
--  Uso: crear la base de datos (CREATE DATABASE smartchefdb;),
--       conectarse a ella y ejecutar este script completo.
--       ATENCIÓN: borra y recrea el esquema "smartchef" entero.
--       En el fichero .env:  DB_SCHEMA=smartchef
-- =====================================================================

-- Se borra el esquema para poder relanzar el script desde cero
DROP SCHEMA IF EXISTS smartchef CASCADE;
CREATE SCHEMA smartchef;
SET search_path TO smartchef;

-- =====================================================================
-- 0. CATÁLOGOS DE OPCIONES
--    Todos tienen la misma estructura:
--      id      SMALLSERIAL -> el valor que se guarda en las tablas (SMALLINT)
--      codigo  constante estable para programar
--      nombre  texto que muestra la app (se puede cambiar libremente)
--      orden   orden en desplegables; activo = FALSE oculta la opción sin borrarla
-- =====================================================================
CREATE TABLE cat_rol_usuario (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_rol_usuario (codigo, nombre, orden) VALUES
    ('USUARIO', 'Usuario', 1),
    ('EDITOR', 'Editor', 2),
    ('ADMIN', 'Administrador', 3);

CREATE TABLE cat_unidad_temperatura (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_unidad_temperatura (codigo, nombre, orden) VALUES
    ('C', 'Grados Celsius (ºC)', 1),
    ('F', 'Grados Fahrenheit (ºF)', 2);

CREATE TABLE cat_unidad_volumen (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_unidad_volumen (codigo, nombre, orden) VALUES
    ('L', 'Litros', 1),
    ('GAL', 'Galones', 2);

CREATE TABLE cat_plataforma (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_plataforma (codigo, nombre, orden) VALUES
    ('ANDROID', 'Android', 1),
    ('IOS', 'iOS', 2),
    ('WEB', 'Web', 3);

-- Región del mundo de la que procede una receta o un ingrediente
CREATE TABLE cat_continente (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_continente (codigo, nombre, orden) VALUES
    ('AMERICA_SUR', 'Sudamérica', 1),
    ('AMERICA_CENTRAL', 'Centroamérica', 2),
    ('AMERICA_NORTE', 'Norteamérica', 3),
    ('AFRICA', 'África', 4),
    ('ASIA', 'Asia', 5),
    ('OCEANIA', 'Oceanía', 6),
    ('EUROPA', 'Europa', 7),
    ('INTERNACIONAL', 'Internacional', 8);

CREATE TABLE cat_dificultad (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_dificultad (codigo, nombre, orden) VALUES
    ('FACIL', 'Fácil', 1),
    ('MEDIA', 'Media', 2),
    ('DIFICIL', 'Difícil', 3),
    ('EXPERTO', 'Experto', 4);

-- Dietas y regímenes de alimentación (del usuario y de las recetas)
CREATE TABLE cat_dieta (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_dieta (codigo, nombre, orden) VALUES
    ('OMNIVORA', 'Omnívora', 1),
    ('VEGETARIANA', 'Vegetariana', 2),
    ('VEGANA', 'Vegana', 3),
    ('PESCETARIANA', 'Pescetariana', 4),
    ('SIN_GLUTEN', 'Sin gluten (celíacos)', 5),
    ('SIN_LACTOSA', 'Sin lactosa', 6),
    ('KETO', 'Cetogénica (keto)', 7),
    ('BAJA_CALORIAS', 'Baja en calorías', 8),
    ('ALTA_PROTEINA', 'Alta en proteínas', 9);

CREATE TABLE cat_tipo_plato (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_tipo_plato (codigo, nombre, orden) VALUES
    ('DESAYUNO', 'Desayuno', 1),
    ('ENTRANTE', 'Entrante', 2),
    ('ENSALADA', 'Ensalada', 3),
    ('SOPA_CREMA', 'Sopa y crema', 4),
    ('PRINCIPAL', 'Plato principal', 5),
    ('POSTRE', 'Postre', 6),
    ('MERIENDA', 'Merienda', 7),
    ('SALSA', 'Salsa', 8),
    ('BEBIDA', 'Bebida', 9);

CREATE TABLE cat_tipo_cocina (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_tipo_cocina (codigo, nombre, orden) VALUES
    ('ESPANOLA', 'Española', 1),
    ('MEDITERRANEA', 'Mediterránea', 2),
    ('ITALIANA', 'Italiana', 3),
    ('MEXICANA', 'Mexicana', 4),
    ('ASIATICA', 'Asiática', 5),
    ('ARABE', 'Árabe', 6),
    ('INTERNACIONAL', 'Internacional', 7);

CREATE TABLE cat_metodo_coccion (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_metodo_coccion (codigo, nombre, orden) VALUES
    ('CRUDO', 'Sin cocción', 1),
    ('HERVIDO', 'Hervido', 2),
    ('SARTEN', 'Sartén / plancha', 3),
    ('HORNO', 'Horno', 4),
    ('VAPOR', 'Al vapor', 5),
    ('GUISADO', 'Guisado / estofado', 6),
    ('PARRILLA', 'Parrilla / barbacoa', 7),
    ('MICROONDAS', 'Microondas', 8),
    ('FREIDORA_AIRE', 'Freidora de aire', 9);

-- Momento del día en el plan de comidas
CREATE TABLE cat_tipo_comida (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_tipo_comida (codigo, nombre, orden) VALUES
    ('DESAYUNO', 'Desayuno', 1),
    ('ALMUERZO', 'Almuerzo (media mañana)', 2),
    ('COMIDA', 'Comida', 3),
    ('MERIENDA', 'Merienda', 4),
    ('CENA', 'Cena', 5);

-- Unidades de las cantidades de una receta, de la despensa y de la lista de la compra
CREATE TABLE cat_unidad_medida (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_unidad_medida (codigo, nombre, orden) VALUES
    ('G', 'gramos (g)', 1),
    ('KG', 'kilos (kg)', 2),
    ('ML', 'mililitros (ml)', 3),
    ('L', 'litros (l)', 4),
    ('UD', 'unidades', 5),
    ('CUCHARADA', 'cucharadas', 6),
    ('CUCHARADITA', 'cucharaditas', 7),
    ('TAZA', 'tazas', 8),
    ('PIZCA', 'pizca', 9),
    ('AL_GUSTO', 'al gusto', 10);

-- Los 14 alérgenos de declaración obligatoria en la Unión Europea
CREATE TABLE cat_alergeno (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_alergeno (codigo, nombre, orden) VALUES
    ('GLUTEN', 'Cereales con gluten', 1),
    ('CRUSTACEOS', 'Crustáceos', 2),
    ('HUEVOS', 'Huevos', 3),
    ('PESCADO', 'Pescado', 4),
    ('CACAHUETES', 'Cacahuetes', 5),
    ('SOJA', 'Soja', 6),
    ('LACTEOS', 'Leche y lácteos (incluye lactosa)', 7),
    ('FRUTOS_CASCARA', 'Frutos de cáscara', 8),
    ('APIO', 'Apio', 9),
    ('MOSTAZA', 'Mostaza', 10),
    ('SESAMO', 'Granos de sésamo', 11),
    ('SULFITOS', 'Dióxido de azufre y sulfitos', 12),
    ('ALTRAMUCES', 'Altramuces', 13),
    ('MOLUSCOS', 'Moluscos', 14);

-- Gravedad de la reacción del usuario a un alérgeno
CREATE TABLE cat_gravedad (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_gravedad (codigo, nombre, orden) VALUES
    ('INTOLERANCIA', 'Intolerancia (molestias digestivas)', 1),
    ('ALERGIA_LEVE', 'Alergia leve', 2),
    ('ALERGIA_GRAVE', 'Alergia grave (riesgo de anafilaxia)', 3);

-- Dónde se guarda un producto de la despensa
CREATE TABLE cat_ubicacion_despensa (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_ubicacion_despensa (codigo, nombre, orden) VALUES
    ('ALACENA', 'Alacena', 1),
    ('NEVERA', 'Nevera', 2),
    ('CONGELADOR', 'Congelador', 3);

-- Por qué sale un producto de la despensa
CREATE TABLE cat_motivo_baja (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_motivo_baja (codigo, nombre, orden) VALUES
    ('CONSUMIDO', 'Consumido', 1),
    ('CADUCADO', 'Caducado', 2),
    ('DESECHADO', 'Desechado (mal estado)', 3),
    ('REGALADO', 'Regalado', 4),
    ('OTRO', 'Otro', 5);

CREATE TABLE cat_categoria_articulo (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_categoria_articulo (codigo, nombre, orden) VALUES
    ('PRIMEROS_PASOS', 'Primeros pasos en la cocina', 1),
    ('TECNICAS', 'Técnicas de cocina', 2),
    ('CONSERVACION', 'Conservación de alimentos', 3),
    ('NUTRICION', 'Nutrición', 4),
    ('ALERGIAS', 'Alergias e intolerancias', 5),
    ('DIETAS', 'Dietas', 6),
    ('PLANIFICACION', 'Planificación y compra', 7),
    ('UTENSILIOS', 'Utensilios', 8),
    ('TRUCOS', 'Trucos', 9);

CREATE TABLE cat_lista_guardada (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL UNIQUE,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    orden        SMALLINT NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO cat_lista_guardada (codigo, nombre, orden) VALUES
    ('FAVORITA', 'Favorita', 1),
    ('PARA_COCINAR', 'Para cocinar', 2);


-- =====================================================================
-- 1. USUARIOS Y AUTENTICACIÓN
-- =====================================================================

CREATE TABLE usuario (
    id                     SERIAL PRIMARY KEY,
    email                  VARCHAR(150) NOT NULL UNIQUE,
    username               VARCHAR(50) NOT NULL UNIQUE,
    password_hash          VARCHAR(255) NOT NULL,              -- BCrypt/Argon2 desde Spring Security
    nombre                 VARCHAR(100),
    apellidos              VARCHAR(150),
    avatar_url             VARCHAR(500),
    id_rol                 SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_rol_usuario(id),   -- 1 = USUARIO
    id_unidad_temperatura  SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_unidad_temperatura(id),   -- 1 = C (horno)
    id_unidad_volumen      SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_unidad_volumen(id),   -- 1 = L
    raciones_por_defecto   SMALLINT NOT NULL DEFAULT 2 CHECK (raciones_por_defecto > 0),
    objetivo_calorias_dia  INTEGER CHECK (objetivo_calorias_dia > 0),   -- opcional, para el plan de comidas
    idioma                 VARCHAR(5) NOT NULL DEFAULT 'es',
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,
    email_verificado       BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_acceso          TIMESTAMP,
    creado_en              TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en         TIMESTAMP NOT NULL DEFAULT now()
);

-- Refresh tokens JWT para la app móvil (se guarda el hash, nunca el token)
CREATE TABLE token_refresco (
    id           SERIAL PRIMARY KEY,
    id_usuario   INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    dispositivo  VARCHAR(150),
    expira_en    TIMESTAMP NOT NULL,
    revocado     BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en    TIMESTAMP NOT NULL DEFAULT now()
);

-- Dispositivos para notificaciones push (FCM / APNs): avisos de plan de comidas y caducidades
CREATE TABLE dispositivo_push (
    id             SERIAL PRIMARY KEY,
    id_usuario     INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_push     VARCHAR(500) NOT NULL UNIQUE,
    id_plataforma  SMALLINT NOT NULL REFERENCES cat_plataforma(id),
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_uso     TIMESTAMP,
    creado_en      TIMESTAMP NOT NULL DEFAULT now()
);

-- Dietas que sigue el usuario (puede seguir varias: vegetariana + sin gluten...)
CREATE TABLE usuario_dieta (
    id_usuario  INTEGER  NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_dieta    SMALLINT NOT NULL REFERENCES cat_dieta(id),
    PRIMARY KEY (id_usuario, id_dieta)
);

-- Alergias e intolerancias del usuario: sirven para avisar o filtrar recetas
CREATE TABLE usuario_alergeno (
    id           SERIAL PRIMARY KEY,
    id_usuario   INTEGER  NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_alergeno  SMALLINT NOT NULL REFERENCES cat_alergeno(id),
    id_gravedad  SMALLINT NOT NULL DEFAULT 2 REFERENCES cat_gravedad(id),   -- 2 = ALERGIA_LEVE
    notas        VARCHAR(255),
    creado_en    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ux_usuario_alergeno UNIQUE (id_usuario, id_alergeno)
);


-- =====================================================================
-- 2. CATÁLOGOS DE INGREDIENTES Y ORIGEN
-- =====================================================================

-- Categorías de ingredientes (verduras, carnes, lácteos...): sirven para filtrar y ordenar la compra
CREATE TABLE categoria_ingrediente (
    id           SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    descripcion  TEXT,
    icono        VARCHAR(50),                             -- nombre de icono para la app
    orden        SMALLINT NOT NULL DEFAULT 0
);

-- Regiones gastronómicas de origen
CREATE TABLE origen (
    id             SERIAL PRIMARY KEY,
    nombre         VARCHAR(150) NOT NULL UNIQUE,           -- "Andalucía", "Cocina de los Andes"
    id_continente  SMALLINT NOT NULL REFERENCES cat_continente(id),
    descripcion    TEXT,
    imagen_url     VARCHAR(500)
);

CREATE TABLE marca (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL UNIQUE,
    pais      VARCHAR(60),
    web_url   VARCHAR(300),
    logo_url  VARCHAR(500)
);


-- =====================================================================
-- 3. ENCICLOPEDIA DE INGREDIENTES (con nutrición y alérgenos)
-- =====================================================================

CREATE TABLE ingrediente (
    id                    SERIAL PRIMARY KEY,
    nombre                VARCHAR(150) NOT NULL UNIQUE,      -- "Tomate", "Aceite de oliva virgen extra"
    slug                  VARCHAR(160) NOT NULL UNIQUE,      -- para URLs amigables / deep links
    id_categoria          INTEGER NOT NULL REFERENCES categoria_ingrediente(id) ON DELETE RESTRICT,
    descripcion           TEXT,

    -- Aptitud para dietas (el servicio Java puede combinarla con los alérgenos)
    apto_vegetariano      BOOLEAN NOT NULL DEFAULT TRUE,
    apto_vegano           BOOLEAN NOT NULL DEFAULT TRUE,

    -- Valores nutricionales POR 100 g (NULL = dato desconocido)
    calorias_kcal         NUMERIC(6,1),
    proteinas_g           NUMERIC(5,2),
    hidratos_g            NUMERIC(5,2),
    azucares_g            NUMERIC(5,2),
    grasas_g              NUMERIC(5,2),
    grasas_saturadas_g    NUMERIC(5,2),
    fibra_g               NUMERIC(5,2),
    sal_g                 NUMERIC(5,2),

    -- Temporada (meses 1-12) y conservación
    temporada_inicio      SMALLINT CHECK (temporada_inicio BETWEEN 1 AND 12),
    temporada_fin         SMALLINT CHECK (temporada_fin BETWEEN 1 AND 12),
    dias_conservacion     SMALLINT CHECK (dias_conservacion > 0),   -- duración estimada una vez comprado

    publicada             BOOLEAN NOT NULL DEFAULT FALSE,    -- flujo editorial: borrador / publicada
    fuentes               TEXT,                              -- bibliografía / referencias (BEDCA, USDA...)
    creado_por            INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    creado_en             TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en        TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT ck_ingrediente_vegano   CHECK (NOT apto_vegano OR apto_vegetariano),
    CONSTRAINT ck_ingrediente_nutri    CHECK ((calorias_kcal IS NULL OR calorias_kcal >= 0)
                                          AND (proteinas_g IS NULL OR proteinas_g >= 0)
                                          AND (hidratos_g IS NULL OR hidratos_g >= 0)
                                          AND (azucares_g IS NULL OR azucares_g >= 0)
                                          AND (grasas_g IS NULL OR grasas_g >= 0)
                                          AND (grasas_saturadas_g IS NULL OR grasas_saturadas_g >= 0)
                                          AND (fibra_g IS NULL OR fibra_g >= 0)
                                          AND (sal_g IS NULL OR sal_g >= 0)),
    CONSTRAINT ck_ingrediente_macros   CHECK (COALESCE(proteinas_g, 0) + COALESCE(hidratos_g, 0)
                                              + COALESCE(grasas_g, 0) <= 100),
    CONSTRAINT ck_ingrediente_azucares CHECK (azucares_g IS NULL OR hidratos_g IS NULL OR azucares_g <= hidratos_g),
    CONSTRAINT ck_ingrediente_saturadas CHECK (grasas_saturadas_g IS NULL OR grasas_g IS NULL
                                               OR grasas_saturadas_g <= grasas_g)
);

-- Alérgenos que contiene un ingrediente
CREATE TABLE ingrediente_alergeno (
    id_ingrediente  INTEGER  NOT NULL REFERENCES ingrediente(id) ON DELETE CASCADE,
    id_alergeno     SMALLINT NOT NULL REFERENCES cat_alergeno(id),
    PRIMARY KEY (id_ingrediente, id_alergeno)
);

CREATE TABLE ingrediente_nombre_alternativo (
    id               SERIAL PRIMARY KEY,
    id_ingrediente   INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE CASCADE,
    nombre           VARCHAR(150) NOT NULL,                -- "jitomate", "tomatoe"...
    idioma           VARCHAR(5) NOT NULL DEFAULT 'es',
    UNIQUE (id_ingrediente, nombre, idioma)
);

-- Galería de imágenes
CREATE TABLE ingrediente_imagen (
    id               SERIAL PRIMARY KEY,
    id_ingrediente   INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE CASCADE,
    url              VARCHAR(500) NOT NULL,
    descripcion      VARCHAR(255),
    autor            VARCHAR(150),
    licencia         VARCHAR(50),                          -- CC BY-SA 4.0, propia...
    es_principal     BOOLEAN NOT NULL DEFAULT FALSE,
    orden            SMALLINT NOT NULL DEFAULT 0
);
-- Solo debe haber una imagen principal por ingrediente: se controla en el servicio Java

CREATE TABLE ingrediente_origen (
    id_ingrediente  INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE CASCADE,
    id_origen       INTEGER NOT NULL REFERENCES origen(id) ON DELETE CASCADE,
    PRIMARY KEY (id_ingrediente, id_origen)
);


-- =====================================================================
-- 4. UTENSILIOS Y ELECTRODOMÉSTICOS
-- =====================================================================

-- Categorías jerárquicas: Electrodomésticos > Horno; Menaje > Sartén...
CREATE TABLE categoria_utensilio (
    id                  SERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    id_categoria_padre  INTEGER REFERENCES categoria_utensilio(id) ON DELETE RESTRICT,
    descripcion         TEXT,
    icono               VARCHAR(50),                        -- nombre de icono para la app
    orden               SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT ux_categoria_utensilio_nombre_padre UNIQUE (nombre, id_categoria_padre)
);

CREATE TABLE utensilio (
    id                SERIAL PRIMARY KEY,
    id_categoria      INTEGER NOT NULL REFERENCES categoria_utensilio(id) ON DELETE RESTRICT,
    id_marca          INTEGER REFERENCES marca(id) ON DELETE RESTRICT,
    modelo            VARCHAR(150) NOT NULL,                -- "Sartén antiadherente 28 cm", "Thermomix TM6"
    descripcion       TEXT NOT NULL,
    potencia_w        NUMERIC(6,1) CHECK (potencia_w > 0),
    capacidad_l       NUMERIC(5,2) CHECK (capacidad_l > 0),
    especificaciones  TEXT,                                 -- datos técnicos en texto libre
    imagen_url        VARCHAR(500),
    creado_en         TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMP NOT NULL DEFAULT now()
);


-- =====================================================================
-- 5. RECETAS
-- =====================================================================

CREATE TABLE receta (
    id                        SERIAL PRIMARY KEY,
    titulo                    VARCHAR(200) NOT NULL,
    slug                      VARCHAR(220) NOT NULL UNIQUE,
    descripcion               TEXT,
    id_tipo_plato             SMALLINT NOT NULL REFERENCES cat_tipo_plato(id),
    id_tipo_cocina            SMALLINT NOT NULL DEFAULT 7 REFERENCES cat_tipo_cocina(id),   -- 7 = INTERNACIONAL
    id_dificultad             SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_dificultad(id),    -- 1 = FACIL
    id_metodo_coccion         SMALLINT REFERENCES cat_metodo_coccion(id),
    id_origen                 INTEGER REFERENCES origen(id) ON DELETE SET NULL,
    raciones                  SMALLINT NOT NULL DEFAULT 2 CHECK (raciones > 0),
    tiempo_preparacion_min    SMALLINT CHECK (tiempo_preparacion_min >= 0),
    tiempo_coccion_min        SMALLINT CHECK (tiempo_coccion_min >= 0),
    imagen_url                VARCHAR(500),
    publicada                 BOOLEAN NOT NULL DEFAULT FALSE,   -- flujo editorial: borrador / publicada
    es_publica                BOOLEAN NOT NULL DEFAULT FALSE,   -- receta de usuario visible para la comunidad
    fuentes                   TEXT,
    creado_por                INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    creado_en                 TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en            TIMESTAMP NOT NULL DEFAULT now()
);

-- Dietas para las que es apta una receta (la marca el autor/editor; el servicio Java puede validarla)
CREATE TABLE receta_dieta (
    id_receta  INTEGER  NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    id_dieta   SMALLINT NOT NULL REFERENCES cat_dieta(id),
    PRIMARY KEY (id_receta, id_dieta)
);

CREATE TABLE receta_ingrediente (
    id                SERIAL PRIMARY KEY,
    id_receta         INTEGER NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    id_ingrediente    INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE RESTRICT,
    cantidad          NUMERIC(8,2) CHECK (cantidad > 0),          -- NULL si la unidad es "al gusto"
    id_unidad         SMALLINT NOT NULL REFERENCES cat_unidad_medida(id),
    cantidad_gramos   NUMERIC(8,2) CHECK (cantidad_gramos > 0),   -- equivalencia en gramos: base del cálculo nutricional
    grupo             VARCHAR(80),                                -- "Para la masa", "Para la salsa"
    notas             VARCHAR(150),                               -- "picada fina", "a temperatura ambiente"
    es_opcional       BOOLEAN NOT NULL DEFAULT FALSE,
    orden             SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE receta_paso (
    id           SERIAL PRIMARY KEY,
    id_receta    INTEGER NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    numero       SMALLINT NOT NULL CHECK (numero > 0),
    descripcion  TEXT NOT NULL,
    duracion_min SMALLINT CHECK (duracion_min >= 0),
    imagen_url   VARCHAR(500),
    CONSTRAINT ux_receta_paso UNIQUE (id_receta, numero)
);

-- Utensilios necesarios para una receta
CREATE TABLE receta_utensilio (
    id_receta     INTEGER NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    id_utensilio  INTEGER NOT NULL REFERENCES utensilio(id) ON DELETE CASCADE,
    PRIMARY KEY (id_receta, id_utensilio)
);

-- Galería de imágenes de la receta
CREATE TABLE receta_imagen (
    id            SERIAL PRIMARY KEY,
    id_receta     INTEGER NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    url           VARCHAR(500) NOT NULL,
    descripcion   VARCHAR(255),
    autor         VARCHAR(150),
    licencia      VARCHAR(50),
    es_principal  BOOLEAN NOT NULL DEFAULT FALSE,
    orden         SMALLINT NOT NULL DEFAULT 0
);


-- =====================================================================
-- 6. DESPENSA Y LISTA DE LA COMPRA DEL USUARIO
-- =====================================================================

-- Un usuario puede tener varias despensas ("Casa", "Casa de la playa")
CREATE TABLE despensa (
    id              SERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nombre          VARCHAR(150) NOT NULL,
    notas           TEXT,
    creado_en       TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ux_despensa_usuario_nombre UNIQUE (id_usuario, nombre)
);

-- Productos que hay en la despensa. Guarda historial: fecha_baja NULL = sigue disponible.
CREATE TABLE despensa_item (
    id                SERIAL PRIMARY KEY,
    id_despensa       INTEGER NOT NULL REFERENCES despensa(id) ON DELETE CASCADE,
    id_ingrediente    INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE RESTRICT,
    id_marca          INTEGER REFERENCES marca(id) ON DELETE SET NULL,
    cantidad          NUMERIC(8,2) NOT NULL CHECK (cantidad > 0),
    id_unidad         SMALLINT NOT NULL REFERENCES cat_unidad_medida(id),
    id_ubicacion      SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_ubicacion_despensa(id),   -- 1 = ALACENA
    fecha_compra      DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_caducidad   DATE,
    fecha_baja        DATE,
    id_motivo_baja    SMALLINT REFERENCES cat_motivo_baja(id),
    notas             VARCHAR(255),
    creado_en         TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_despensa_item_baja   CHECK ((fecha_baja IS NULL AND id_motivo_baja IS NULL)
                                           OR (fecha_baja IS NOT NULL AND id_motivo_baja IS NOT NULL)),
    CONSTRAINT ck_despensa_item_fechas CHECK (fecha_baja IS NULL OR fecha_baja >= fecha_compra)
);

CREATE TABLE lista_compra (
    id              SERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nombre          VARCHAR(150) NOT NULL,
    completada      BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en       TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE lista_compra_item (
    id                 SERIAL PRIMARY KEY,
    id_lista           INTEGER NOT NULL REFERENCES lista_compra(id) ON DELETE CASCADE,
    id_ingrediente     INTEGER REFERENCES ingrediente(id) ON DELETE RESTRICT,
    descripcion_libre  VARCHAR(150),                       -- si el producto no está en el catálogo
    cantidad           NUMERIC(8,2) CHECK (cantidad > 0),
    id_unidad          SMALLINT REFERENCES cat_unidad_medida(id),
    id_receta          INTEGER REFERENCES receta(id) ON DELETE SET NULL,   -- receta que originó la línea
    comprado           BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_lista_item_origen CHECK (id_ingrediente IS NOT NULL OR descripcion_libre IS NOT NULL)
);

-- Utensilios que tiene el usuario en su cocina
CREATE TABLE usuario_utensilio (
    id                 SERIAL PRIMARY KEY,
    id_usuario         INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_utensilio       INTEGER REFERENCES utensilio(id) ON DELETE RESTRICT,
    descripcion_libre  VARCHAR(150),                       -- si el utensilio no está en el catálogo
    cantidad           SMALLINT NOT NULL DEFAULT 1 CHECK (cantidad >= 1),
    fecha_adquisicion  DATE,
    notas              VARCHAR(255),
    CONSTRAINT ck_usuario_utensilio_origen CHECK (id_utensilio IS NOT NULL OR descripcion_libre IS NOT NULL)
);


-- =====================================================================
-- 7. PLAN DE COMIDAS Y DIARIO DE COCINA
-- =====================================================================

-- Qué se va a comer cada día. Genera avisos push si notificar = TRUE.
CREATE TABLE plan_comida (
    id                 SERIAL PRIMARY KEY,
    id_usuario         INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    fecha              DATE NOT NULL,
    id_tipo_comida     SMALLINT NOT NULL REFERENCES cat_tipo_comida(id),
    id_receta          INTEGER REFERENCES receta(id) ON DELETE SET NULL,
    descripcion_libre  VARCHAR(150),                       -- "Cena fuera", "Sobras de ayer"
    raciones           SMALLINT NOT NULL DEFAULT 1 CHECK (raciones > 0),
    hora_aviso         TIME,
    notificar          BOOLEAN NOT NULL DEFAULT FALSE,
    completada         BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en          TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_plan_comida_origen CHECK (id_receta IS NOT NULL OR descripcion_libre IS NOT NULL)
);

-- Bitácora del usuario: lo que cocinó, qué tal salió, con fotos
CREATE TABLE diario_cocina (
    id              SERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_receta       INTEGER REFERENCES receta(id) ON DELETE SET NULL,
    fecha           TIMESTAMP NOT NULL DEFAULT now(),
    titulo          VARCHAR(150),
    contenido       TEXT NOT NULL,
    valoracion      SMALLINT CHECK (valoracion BETWEEN 1 AND 5),
    creado_en       TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE diario_foto (
    id           SERIAL PRIMARY KEY,
    id_diario    INTEGER NOT NULL REFERENCES diario_cocina(id) ON DELETE CASCADE,
    url          VARCHAR(500) NOT NULL,
    descripcion  VARCHAR(255),
    tomada_en    TIMESTAMP NOT NULL DEFAULT now()
);


-- =====================================================================
-- 8. CONTENIDO DIVULGATIVO Y RECETAS GUARDADAS
-- =====================================================================

CREATE TABLE articulo (
    id                  SERIAL PRIMARY KEY,
    titulo              VARCHAR(200) NOT NULL,
    slug                VARCHAR(220) NOT NULL UNIQUE,
    id_categoria        SMALLINT NOT NULL REFERENCES cat_categoria_articulo(id),
    resumen             VARCHAR(500),
    contenido_md        TEXT NOT NULL,              -- Markdown: fácil de renderizar en móvil
    imagen_portada_url  VARCHAR(500),
    id_autor            INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    publicado           BOOLEAN NOT NULL DEFAULT FALSE,
    publicado_en        TIMESTAMP,
    creado_en           TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE articulo_ingrediente (
    id_articulo     INTEGER NOT NULL REFERENCES articulo(id) ON DELETE CASCADE,
    id_ingrediente  INTEGER NOT NULL REFERENCES ingrediente(id) ON DELETE CASCADE,
    PRIMARY KEY (id_articulo, id_ingrediente)
);

CREATE TABLE glosario_termino (
    id           SERIAL PRIMARY KEY,
    termino      VARCHAR(100) NOT NULL UNIQUE,           -- "Sofrito", "Al dente", "Blanquear"
    definicion   TEXT NOT NULL,
    id_articulo  INTEGER REFERENCES articulo(id) ON DELETE SET NULL
);

-- Recetas favoritas y "para cocinar" del usuario
CREATE TABLE usuario_receta_guardada (
    id          SERIAL PRIMARY KEY,
    id_usuario  INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_receta   INTEGER NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    id_lista    SMALLINT NOT NULL DEFAULT 1 REFERENCES cat_lista_guardada(id),   -- 1 = FAVORITA
    creado_en   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ux_usuario_receta_guardada UNIQUE (id_usuario, id_receta, id_lista)
);


-- =====================================================================
-- 9. VISTAS DE AYUDA (solo lectura, no se mapean con entidades)
--    Se pueden sustituir por cálculos en los servicios Java (sección 11).
-- =====================================================================

-- Nutrición POR RACIÓN de cada receta (solo líneas con cantidad_gramos y no opcionales)
CREATE VIEW v_receta_nutricion AS
SELECT r.id AS id_receta,
       r.titulo,
       r.raciones,
       ROUND(SUM(ri.cantidad_gramos * i.calorias_kcal       / 100.0) / r.raciones, 1) AS calorias_kcal,
       ROUND(SUM(ri.cantidad_gramos * i.proteinas_g         / 100.0) / r.raciones, 1) AS proteinas_g,
       ROUND(SUM(ri.cantidad_gramos * i.hidratos_g          / 100.0) / r.raciones, 1) AS hidratos_g,
       ROUND(SUM(ri.cantidad_gramos * i.azucares_g          / 100.0) / r.raciones, 1) AS azucares_g,
       ROUND(SUM(ri.cantidad_gramos * i.grasas_g            / 100.0) / r.raciones, 1) AS grasas_g,
       ROUND(SUM(ri.cantidad_gramos * i.grasas_saturadas_g  / 100.0) / r.raciones, 1) AS grasas_saturadas_g,
       ROUND(SUM(ri.cantidad_gramos * i.fibra_g             / 100.0) / r.raciones, 1) AS fibra_g,
       ROUND(SUM(ri.cantidad_gramos * i.sal_g               / 100.0) / r.raciones, 2) AS sal_g
FROM receta r
JOIN receta_ingrediente ri ON ri.id_receta = r.id
                          AND ri.cantidad_gramos IS NOT NULL
                          AND ri.es_opcional = FALSE
JOIN ingrediente i ON i.id = ri.id_ingrediente
GROUP BY r.id, r.titulo, r.raciones;

-- Alérgenos de cada receta, deducidos de sus ingredientes (incluye los opcionales, por seguridad)
CREATE VIEW v_receta_alergeno AS
SELECT DISTINCT ri.id_receta, ia.id_alergeno
FROM receta_ingrediente ri
JOIN ingrediente_alergeno ia ON ia.id_ingrediente = ri.id_ingrediente;


-- =====================================================================
-- 10. DATOS DE EJEMPLO
--     · Las opciones de catálogo se buscan por su código con una subconsulta.
--     · Los valores nutricionales son orientativos (por 100 g);
--       revisar con BEDCA / USDA antes de producción.
-- =====================================================================

-- ---------- Categorías de ingredientes ----------
INSERT INTO categoria_ingrediente (nombre, icono, orden) VALUES
    ('Verduras y hortalizas',  'carrot',   1),
    ('Frutas',                 'apple',    2),
    ('Carnes',                 'meat',     3),
    ('Pescados y mariscos',    'fish',     4),
    ('Lácteos y huevos',       'milk',     5),
    ('Cereales, pasta y arroz','wheat',    6),
    ('Legumbres',              'bean',     7),
    ('Frutos secos',           'nut',      8),
    ('Aceites y grasas',       'oil',      9),
    ('Especias y hierbas',     'herb',    10);

-- ---------- Orígenes ----------
INSERT INTO origen (nombre, id_continente, descripcion) VALUES
    ('Andalucía',
        (SELECT id FROM cat_continente WHERE codigo = 'EUROPA'),
        'Cocina del sur de España: aceite de oliva, verduras de temporada, fritura y platos fríos como el gazpacho.'),
    ('Italia',
        (SELECT id FROM cat_continente WHERE codigo = 'EUROPA'),
        'Pasta, tomate, aceite de oliva y quesos curados son la base de su despensa.'),
    ('México',
        (SELECT id FROM cat_continente WHERE codigo = 'AMERICA_CENTRAL'),
        'Maíz, chile, frijoles y tomate; origen de muchos ingredientes hoy universales.'),
    ('Japón',
        (SELECT id FROM cat_continente WHERE codigo = 'ASIA'),
        'Arroz, pescado, soja y caldos; énfasis en el producto fresco y la sencillez.');

-- ---------- Marcas ----------
INSERT INTO marca (nombre, pais) VALUES
    ('Genérica', 'España'),
    ('Tefal', 'Francia'),
    ('Bosch', 'Alemania');

-- ---------- Ingredientes (valores por 100 g) ----------
INSERT INTO ingrediente (nombre, slug, id_categoria, descripcion, apto_vegetariano, apto_vegano,
                         calorias_kcal, proteinas_g, hidratos_g, azucares_g, grasas_g, grasas_saturadas_g,
                         fibra_g, sal_g, temporada_inicio, temporada_fin, dias_conservacion, publicada) VALUES
    ('Tomate', 'tomate',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Verduras y hortalizas'),
        'Fruto de la tomatera, base de salsas, ensaladas y guisos.',
        TRUE, TRUE, 18, 0.90, 3.90, 2.60, 0.20, 0.00, 1.20, 0.01, 6, 9, 7, TRUE),
    ('Cebolla', 'cebolla',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Verduras y hortalizas'),
        'Bulbo base de sofritos; se puede comer cruda o cocinada.',
        TRUE, TRUE, 40, 1.10, 9.30, 4.20, 0.10, 0.00, 1.70, 0.01, NULL, NULL, 30, TRUE),
    ('Ajo', 'ajo',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Especias y hierbas'),
        'Bulbo aromático imprescindible en la cocina mediterránea.',
        TRUE, TRUE, 149, 6.40, 33.10, 1.00, 0.50, 0.10, 2.10, 0.02, NULL, NULL, 60, TRUE),
    ('Aceite de oliva virgen extra', 'aceite-de-oliva-virgen-extra',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Aceites y grasas'),
        'Aceite obtenido por procedimientos mecánicos de la aceituna.',
        TRUE, TRUE, 884, 0.00, 0.00, 0.00, 100.00, 14.00, 0.00, 0.00, NULL, NULL, 540, TRUE),
    ('Huevo', 'huevo',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Lácteos y huevos'),
        'Huevo de gallina, categoría M.',
        TRUE, FALSE, 143, 12.60, 0.70, 0.40, 9.50, 3.10, 0.00, 0.36, NULL, NULL, 28, TRUE),
    ('Leche entera', 'leche-entera',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Lácteos y huevos'),
        'Leche de vaca entera, pasteurizada o UHT.',
        TRUE, FALSE, 61, 3.20, 4.80, 4.80, 3.30, 1.90, 0.00, 0.10, NULL, NULL, 5, TRUE),
    ('Queso parmesano', 'queso-parmesano',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Lácteos y huevos'),
        'Queso duro madurado, se usa rallado. Se elabora con cuajo animal.',
        FALSE, FALSE, 392, 35.80, 3.20, 0.80, 25.80, 16.00, 0.00, 1.60, NULL, NULL, 60, TRUE),
    ('Pechuga de pollo', 'pechuga-de-pollo',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Carnes'),
        'Pechuga de pollo sin piel, cruda.',
        FALSE, FALSE, 120, 22.50, 0.00, 0.00, 2.60, 0.60, 0.00, 0.13, NULL, NULL, 2, TRUE),
    ('Salmón', 'salmon',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Pescados y mariscos'),
        'Pescado azul rico en omega-3.',
        FALSE, FALSE, 208, 20.40, 0.00, 0.00, 13.40, 3.10, 0.00, 0.10, NULL, NULL, 2, TRUE),
    ('Arroz blanco', 'arroz-blanco',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Cereales, pasta y arroz'),
        'Arroz blanco de grano medio, crudo.',
        TRUE, TRUE, 360, 6.70, 79.30, 0.10, 0.60, 0.20, 1.30, 0.01, NULL, NULL, 720, TRUE),
    ('Espaguetis', 'espaguetis',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Cereales, pasta y arroz'),
        'Pasta seca de trigo duro.',
        TRUE, TRUE, 359, 12.50, 72.00, 3.50, 1.50, 0.30, 3.00, 0.01, NULL, NULL, 720, TRUE),
    ('Harina de trigo', 'harina-de-trigo',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Cereales, pasta y arroz'),
        'Harina de trigo común.',
        TRUE, TRUE, 364, 10.30, 76.30, 0.30, 1.00, 0.20, 2.70, 0.01, NULL, NULL, 365, TRUE),
    ('Garbanzos cocidos', 'garbanzos-cocidos',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Legumbres'),
        'Garbanzos ya cocidos, en conserva o de cocción casera.',
        TRUE, TRUE, 164, 8.90, 27.40, 4.80, 2.60, 0.30, 7.60, 0.02, NULL, NULL, 4, TRUE),
    ('Almendras', 'almendras',
        (SELECT id FROM categoria_ingrediente WHERE nombre = 'Frutos secos'),
        'Fruto seco crudo, con o sin piel.',
        TRUE, TRUE, 579, 21.20, 21.60, 4.40, 49.90, 3.80, 12.50, 0.01, NULL, NULL, 180, TRUE);

-- ---------- Alérgenos de cada ingrediente ----------
INSERT INTO ingrediente_alergeno (id_ingrediente, id_alergeno) VALUES
    ((SELECT id FROM ingrediente WHERE nombre = 'Huevo'),           (SELECT id FROM cat_alergeno WHERE codigo = 'HUEVOS')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Leche entera'),    (SELECT id FROM cat_alergeno WHERE codigo = 'LACTEOS')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Queso parmesano'), (SELECT id FROM cat_alergeno WHERE codigo = 'LACTEOS')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Salmón'),          (SELECT id FROM cat_alergeno WHERE codigo = 'PESCADO')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Espaguetis'),      (SELECT id FROM cat_alergeno WHERE codigo = 'GLUTEN')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Harina de trigo'), (SELECT id FROM cat_alergeno WHERE codigo = 'GLUTEN')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Almendras'),       (SELECT id FROM cat_alergeno WHERE codigo = 'FRUTOS_CASCARA'));

-- ---------- Orígenes de cada ingrediente ----------
INSERT INTO ingrediente_origen (id_ingrediente, id_origen) VALUES
    ((SELECT id FROM ingrediente WHERE nombre = 'Aceite de oliva virgen extra'), (SELECT id FROM origen WHERE nombre = 'Andalucía')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Tomate'),                       (SELECT id FROM origen WHERE nombre = 'México')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Espaguetis'),                   (SELECT id FROM origen WHERE nombre = 'Italia')),
    ((SELECT id FROM ingrediente WHERE nombre = 'Queso parmesano'),              (SELECT id FROM origen WHERE nombre = 'Italia'));

-- ---------- Nombres alternativos ----------
INSERT INTO ingrediente_nombre_alternativo (id_ingrediente, nombre, idioma) VALUES
    ((SELECT id FROM ingrediente WHERE nombre = 'Tomate'),            'Jitomate', 'es'),
    ((SELECT id FROM ingrediente WHERE nombre = 'Tomate'),            'Tomato',   'en'),
    ((SELECT id FROM ingrediente WHERE nombre = 'Garbanzos cocidos'), 'Chickpeas', 'en'),
    ((SELECT id FROM ingrediente WHERE nombre = 'Espaguetis'),        'Spaghetti','en');

-- ---------- Categorías de utensilios ----------
INSERT INTO categoria_utensilio (nombre, icono, orden) VALUES
    ('Electrodomésticos', 'plug',   1),
    ('Menaje de cocción', 'pot',    2),
    ('Corte y preparación','knife', 3),
    ('Medición',          'scale',  4);

-- Subcategorías: su padre se busca por nombre
INSERT INTO categoria_utensilio (nombre, id_categoria_padre, orden) VALUES
    ('Horno',      (SELECT id FROM categoria_utensilio WHERE nombre = 'Electrodomésticos' AND id_categoria_padre IS NULL), 1),
    ('Batidora',   (SELECT id FROM categoria_utensilio WHERE nombre = 'Electrodomésticos' AND id_categoria_padre IS NULL), 2),
    ('Sartenes',   (SELECT id FROM categoria_utensilio WHERE nombre = 'Menaje de cocción' AND id_categoria_padre IS NULL), 1),
    ('Ollas',      (SELECT id FROM categoria_utensilio WHERE nombre = 'Menaje de cocción' AND id_categoria_padre IS NULL), 2),
    ('Cuchillos',  (SELECT id FROM categoria_utensilio WHERE nombre = 'Corte y preparación' AND id_categoria_padre IS NULL), 1),
    ('Básculas',   (SELECT id FROM categoria_utensilio WHERE nombre = 'Medición' AND id_categoria_padre IS NULL), 1);

-- ---------- Utensilios ----------
INSERT INTO utensilio (id_categoria, id_marca, modelo, descripcion, potencia_w, capacidad_l) VALUES
    ((SELECT id FROM categoria_utensilio WHERE nombre = 'Sartenes'),
     (SELECT id FROM marca WHERE nombre = 'Tefal'),
     'Sartén antiadherente 28 cm', 'Sartén antiadherente apta para todo tipo de cocinas, incluida inducción.', NULL, NULL),
    ((SELECT id FROM categoria_utensilio WHERE nombre = 'Ollas'),
     (SELECT id FROM marca WHERE nombre = 'Genérica'),
     'Olla alta 24 cm', 'Olla de acero inoxidable para pasta, sopas y cocidos.', NULL, 6.50),
    ((SELECT id FROM categoria_utensilio WHERE nombre = 'Horno'),
     (SELECT id FROM marca WHERE nombre = 'Bosch'),
     'Horno multifunción', 'Horno de convección con calor arriba/abajo, grill y ventilador.', 3400, 71.00);

-- ---------- Recetas ----------
INSERT INTO receta (titulo, slug, descripcion, id_tipo_plato, id_tipo_cocina, id_dificultad, id_metodo_coccion,
                    id_origen, raciones, tiempo_preparacion_min, tiempo_coccion_min, publicada) VALUES
    ('Espaguetis al pomodoro', 'espaguetis-al-pomodoro',
        'Pasta con salsa de tomate natural, ajo y aceite de oliva. Rápida, barata y vegana si se omite el parmesano.',
        (SELECT id FROM cat_tipo_plato WHERE codigo = 'PRINCIPAL'),
        (SELECT id FROM cat_tipo_cocina WHERE codigo = 'ITALIANA'),
        (SELECT id FROM cat_dificultad WHERE codigo = 'FACIL'),
        (SELECT id FROM cat_metodo_coccion WHERE codigo = 'HERVIDO'),
        (SELECT id FROM origen WHERE nombre = 'Italia'),
        2, 10, 20, TRUE),
    ('Pollo al ajillo con arroz', 'pollo-al-ajillo-con-arroz',
        'Taquitos de pollo dorados con mucho ajo, acompañados de arroz blanco.',
        (SELECT id FROM cat_tipo_plato WHERE codigo = 'PRINCIPAL'),
        (SELECT id FROM cat_tipo_cocina WHERE codigo = 'ESPANOLA'),
        (SELECT id FROM cat_dificultad WHERE codigo = 'MEDIA'),
        (SELECT id FROM cat_metodo_coccion WHERE codigo = 'SARTEN'),
        (SELECT id FROM origen WHERE nombre = 'Andalucía'),
        2, 15, 30, TRUE),
    ('Ensalada de garbanzos', 'ensalada-de-garbanzos',
        'Ensalada fría de legumbre con tomate y cebolla, aliñada con aceite de oliva. Sin cocinar.',
        (SELECT id FROM cat_tipo_plato WHERE codigo = 'ENSALADA'),
        (SELECT id FROM cat_tipo_cocina WHERE codigo = 'MEDITERRANEA'),
        (SELECT id FROM cat_dificultad WHERE codigo = 'FACIL'),
        (SELECT id FROM cat_metodo_coccion WHERE codigo = 'CRUDO'),
        (SELECT id FROM origen WHERE nombre = 'Andalucía'),
        2, 10, 0, TRUE);

-- ---------- Ingredientes de cada receta ----------
INSERT INTO receta_ingrediente (id_receta, id_ingrediente, cantidad, id_unidad, cantidad_gramos, grupo, notas, es_opcional, orden) VALUES
    -- Espaguetis al pomodoro
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM ingrediente WHERE nombre = 'Espaguetis'),
        200, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 200, NULL, NULL, FALSE, 1),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM ingrediente WHERE nombre = 'Tomate'),
        400, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 400, 'Para la salsa', 'maduros, pelados y troceados', FALSE, 2),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM ingrediente WHERE nombre = 'Ajo'),
        2, (SELECT id FROM cat_unidad_medida WHERE codigo = 'UD'), 8, 'Para la salsa', 'dientes laminados', FALSE, 3),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM ingrediente WHERE nombre = 'Aceite de oliva virgen extra'),
        2, (SELECT id FROM cat_unidad_medida WHERE codigo = 'CUCHARADA'), 18, 'Para la salsa', NULL, FALSE, 4),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM ingrediente WHERE nombre = 'Queso parmesano'),
        20, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 20, NULL, 'rallado, para servir', TRUE, 5),
    -- Pollo al ajillo con arroz
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM ingrediente WHERE nombre = 'Pechuga de pollo'),
        400, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 400, NULL, 'en taquitos', FALSE, 1),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM ingrediente WHERE nombre = 'Ajo'),
        6, (SELECT id FROM cat_unidad_medida WHERE codigo = 'UD'), 24, NULL, 'dientes con piel, aplastados', FALSE, 2),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM ingrediente WHERE nombre = 'Aceite de oliva virgen extra'),
        45, (SELECT id FROM cat_unidad_medida WHERE codigo = 'ML'), 41, NULL, NULL, FALSE, 3),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM ingrediente WHERE nombre = 'Arroz blanco'),
        150, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 150, 'Guarnición', NULL, FALSE, 4),
    -- Ensalada de garbanzos
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), (SELECT id FROM ingrediente WHERE nombre = 'Garbanzos cocidos'),
        400, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), 400, NULL, 'escurridos y lavados', FALSE, 1),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), (SELECT id FROM ingrediente WHERE nombre = 'Tomate'),
        2, (SELECT id FROM cat_unidad_medida WHERE codigo = 'UD'), 250, NULL, 'en dados', FALSE, 2),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), (SELECT id FROM ingrediente WHERE nombre = 'Cebolla'),
        1, (SELECT id FROM cat_unidad_medida WHERE codigo = 'UD'), 100, NULL, 'picada muy fina', FALSE, 3),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), (SELECT id FROM ingrediente WHERE nombre = 'Aceite de oliva virgen extra'),
        3, (SELECT id FROM cat_unidad_medida WHERE codigo = 'CUCHARADA'), 27, 'Aliño', NULL, FALSE, 4);

-- ---------- Pasos de cada receta ----------
INSERT INTO receta_paso (id_receta, numero, descripcion, duracion_min) VALUES
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), 1,
        'Pon a hervir abundante agua con sal en una olla alta.', 10),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), 2,
        'Mientras tanto, sofríe el ajo laminado en aceite a fuego medio sin que se queme y añade el tomate. Cocina 15 minutos.', 15),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), 3,
        'Cuece los espaguetis según el paquete, escúrrelos y mézclalos con la salsa. Sirve con parmesano si quieres.', 10),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), 1,
        'Cuece el arroz en agua con sal durante 15-18 minutos, escúrrelo y resérvalo.', 18),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), 2,
        'Sazona el pollo y dóralo en la sartén con el aceite bien caliente.', 8),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), 3,
        'Baja el fuego, añade los ajos aplastados y cocina 10 minutos más hasta que el pollo esté hecho. Sirve con el arroz.', 10),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), 1,
        'Mezcla en un bol los garbanzos, el tomate en dados y la cebolla picada.', 5),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'), 2,
        'Aliña con aceite, sal y vinagre al gusto y deja reposar 10 minutos en la nevera.', 10);

-- ---------- Dietas aptas por receta ----------
INSERT INTO receta_dieta (id_receta, id_dieta) VALUES
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'), (SELECT id FROM cat_dieta WHERE codigo = 'VEGETARIANA')),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'),  (SELECT id FROM cat_dieta WHERE codigo = 'VEGETARIANA')),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'),  (SELECT id FROM cat_dieta WHERE codigo = 'VEGANA')),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'),  (SELECT id FROM cat_dieta WHERE codigo = 'SIN_GLUTEN')),
    ((SELECT id FROM receta WHERE slug = 'ensalada-de-garbanzos'),  (SELECT id FROM cat_dieta WHERE codigo = 'SIN_LACTOSA')),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM cat_dieta WHERE codigo = 'SIN_GLUTEN')),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM cat_dieta WHERE codigo = 'SIN_LACTOSA')),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM cat_dieta WHERE codigo = 'ALTA_PROTEINA'));

-- ---------- Utensilios de cada receta ----------
INSERT INTO receta_utensilio (id_receta, id_utensilio) VALUES
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'),    (SELECT id FROM utensilio WHERE modelo = 'Olla alta 24 cm')),
    ((SELECT id FROM receta WHERE slug = 'espaguetis-al-pomodoro'),    (SELECT id FROM utensilio WHERE modelo = 'Sartén antiadherente 28 cm')),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM utensilio WHERE modelo = 'Olla alta 24 cm')),
    ((SELECT id FROM receta WHERE slug = 'pollo-al-ajillo-con-arroz'), (SELECT id FROM utensilio WHERE modelo = 'Sartén antiadherente 28 cm'));

-- ---------- Glosario básico ----------
INSERT INTO glosario_termino (termino, definicion) VALUES
    ('Sofrito', 'Base de cocina que consiste en cocinar lentamente verduras (cebolla, ajo, tomate...) en aceite antes de añadir el resto de ingredientes.'),
    ('Al dente', 'Punto de cocción de la pasta o el arroz en el que aún ofrece una ligera resistencia al morder.'),
    ('Blanquear', 'Sumergir un alimento unos segundos en agua hirviendo y pasarlo enseguida a agua helada para frenar la cocción.'),
    ('Mise en place', 'Tener todos los ingredientes medidos, cortados y preparados antes de empezar a cocinar.'),
    ('Alérgeno', 'Sustancia presente en un alimento que puede provocar una reacción del sistema inmunitario. En la UE hay 14 de declaración obligatoria.');


-- =====================================================================
-- 11. LÓGICA QUE SE PROGRAMA EN JAVA (ideas de ejercicios para los servicios)
-- =====================================================================
--  · Filtro por alergias: excluir las recetas cuyo alérgeno (v_receta_alergeno)
--    coincida con los alérgenos del usuario (usuario_alergeno).
--  · Filtro por dieta: mostrar solo recetas con receta_dieta = alguna de las dietas
--    del usuario (usuario_dieta).
--  · Nutrición: sumar cantidad_gramos * valor_por_100g / 100 de cada línea y dividir
--    por las raciones (ver v_receta_nutricion). Escalar si el usuario cambia las raciones.
--  · Qué puedo cocinar hoy: recetas cuyos ingredientes no opcionales están todos
--    en despensa_item con fecha_baja NULL.
--  · Lista de la compra: al añadir una receta, insertar los ingredientes que NO estén
--    en la despensa del usuario.
--  · Caducidades: avisar (push) de despensa_item con fecha_caducidad próxima.
--  · Plan de comidas: sumar las calorías del día y compararlas con
--    usuario.objetivo_calorias_dia.
--  · Imagen principal: al marcar una como principal, desmarcar las demás.
--  · Buscador: repositorio con findByTituloContainingIgnoreCase(...).
-- =====================================================================

-- Fin del script
