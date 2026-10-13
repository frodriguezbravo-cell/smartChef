-- =====================================================================
--  SMARTCHEF · Usuario de prueba para probar la API (sin login todavía)
--  Ejecutar DESPUÉS de script_smartchefdb.sql.
--  Crea: el usuario "demo" (id 1 si la base de datos es nueva), una despensa
--  "Casa" y una lista de la compra. El hash de contraseña NO es real: cuando
--  se añada el login (BCrypt) habrá que registrar usuarios de verdad.
-- =====================================================================
SET search_path TO smartchef;

INSERT INTO usuario (email, username, password_hash, nombre, objetivo_calorias_dia)
VALUES ('demo@smartchef.es', 'demo', 'SIN_LOGIN_TODAVIA', 'Usuario de prueba', 2000);

INSERT INTO despensa (id_usuario, nombre)
VALUES ((SELECT id FROM usuario WHERE username = 'demo'), 'Casa');

INSERT INTO lista_compra (id_usuario, nombre)
VALUES ((SELECT id FROM usuario WHERE username = 'demo'), 'Compra de la semana');
