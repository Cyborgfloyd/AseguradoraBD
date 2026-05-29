
CREATE DATABASE segurosmurillo CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE segurosmurillo;

--  TABLAS

-- Catálogo de tipos de cobertura Amplia, Limitada, Responsabilidad Civil
CREATE TABLE tipos_cobertura (
    id_cobertura     INT AUTO_INCREMENT,
    nombre_cobertura VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT pk_tipos_cobertura PRIMARY KEY (id_cobertura)
) ;

-- Usuarios del sistema con roles Administrador / Operador
CREATE TABLE usuarios_sistema (
    id_usuario    INT AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           ENUM('Administrador', 'Operador') DEFAULT 'Operador',
    CONSTRAINT pk_usuarios_sistema PRIMARY KEY (id_usuario)
);

-- Clientes 
CREATE TABLE clientes (
    id_cliente  INT AUTO_INCREMENT,
    nombre      VARCHAR(150) NOT NULL,
    apellido_p  VARCHAR(50)  NULL,
    apellido_m  VARCHAR(50)  NULL,
    telefono_1  VARCHAR(15)  NOT NULL,
    telefono_2  VARCHAR(15)  NULL,
    correo      VARCHAR(100) NULL,
    estado      ENUM('Activo', 'Inactivo') DEFAULT 'Activo',
    CONSTRAINT pk_clientes PRIMARY KEY (id_cliente)
);

-- Vehículos
CREATE TABLE vehiculos (
    id_vehiculo INT AUTO_INCREMENT,
    id_cliente  INT         NOT NULL,
    modelo      VARCHAR(100) NOT NULL,
    placas      VARCHAR(15)  NOT NULL,
    estado      ENUM('Activo', 'Inactivo') DEFAULT 'Activo',
    CONSTRAINT pk_vehiculos PRIMARY KEY (id_vehiculo),
    CONSTRAINT fk_vehiculos_clientes
        FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Compañías aseguradoras disponibles
CREATE TABLE companias (
    id_compania INT AUTO_INCREMENT,
    compania    VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT pk_companias PRIMARY KEY (id_compania)
);

-- Pólizas de seguro emitidas
CREATE TABLE polizas (
    no_poliza   VARCHAR(50) NOT NULL,
    id_cliente  INT         NOT NULL,
    id_vehiculo INT         NOT NULL,
    id_compania INT         NOT NULL,
    id_cobertura INT        NOT NULL,
    fecha_inicio DATE       NOT NULL,
    cfdi        BOOLEAN     DEFAULT FALSE,
    estatus     ENUM('Activa', 'Vencida', 'Cancelada') DEFAULT 'Activa',
    CONSTRAINT pk_polizas PRIMARY KEY (no_poliza),
    CONSTRAINT fk_polizas_clientes
        FOREIGN KEY (id_cliente)   REFERENCES clientes(id_cliente)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_polizas_vehiculos
        FOREIGN KEY (id_vehiculo)  REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_polizas_companias
        FOREIGN KEY (id_compania)  REFERENCES companias(id_compania)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_polizas_coberturas
        FOREIGN KEY (id_cobertura) REFERENCES tipos_cobertura(id_cobertura)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Parcialidades de pago por póliza
CREATE TABLE pagos_parcialidades (
    id_pago             INT AUTO_INCREMENT,
    no_poliza           VARCHAR(50) NOT NULL,
    num_parcialidad     INT         NOT NULL,
    total_parcialidades INT         NOT NULL,
    fecha_vencimiento   DATE        NOT NULL,
    estatus             ENUM('Pendiente', 'Pagado', 'Vencido') DEFAULT 'Pendiente',
    CONSTRAINT pk_pagos_parcialidades PRIMARY KEY (id_pago),
    CONSTRAINT fk_pagos_polizas
        FOREIGN KEY (no_poliza) REFERENCES polizas(no_poliza)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Clientes dados de baja (espejo de clientes para bajas definitivas)
CREATE TABLE clientes_historico (
    id_cliente  INT,
    nombre      VARCHAR(150),
    apellido_p  VARCHAR(50),
    apellido_m  VARCHAR(50),
    telefono_1  VARCHAR(15),
    telefono_2  VARCHAR(15),
    correo      VARCHAR(100),
    estado      ENUM('Activo', 'Inactivo'),
    fecha_baja  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Historial
CREATE TABLE historial (
    id_log  INT AUTO_INCREMENT,
    usuario VARCHAR(100),
    accion  VARCHAR(50),
    tabla   VARCHAR(50),
    fecha   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    detalles TEXT,
    CONSTRAINT pk_auditoria PRIMARY KEY (id_log)
) ENGINE=InnoDB;


-- TRIGGERS

DELIMITER $$

-- Normaliza nombre y apellidos a mayúsculas al insertar un cliente
CREATE TRIGGER tg_formatear_cliente_mayusculas
BEFORE INSERT ON clientes
FOR EACH ROW
BEGIN
    SET NEW.nombre     = UPPER(TRIM(NEW.nombre));
    IF NEW.apellido_p IS NOT NULL THEN
        SET NEW.apellido_p = UPPER(TRIM(NEW.apellido_p));
    END IF;
    IF NEW.apellido_m IS NOT NULL THEN
        SET NEW.apellido_m = UPPER(TRIM(NEW.apellido_m));
    END IF;
END$$

-- Normaliza nombre y apellidos a mayúsculas al actualizar un cliente
CREATE TRIGGER tg_formatear_cliente_update
BEFORE UPDATE ON clientes
FOR EACH ROW
BEGIN
    SET NEW.nombre     = UPPER(TRIM(NEW.nombre));
    IF NEW.apellido_p IS NOT NULL THEN
        SET NEW.apellido_p = UPPER(TRIM(NEW.apellido_p));
    END IF;
    IF NEW.apellido_m IS NOT NULL THEN
        SET NEW.apellido_m = UPPER(TRIM(NEW.apellido_m));
    END IF;
END$$

-- Normaliza placas y modelo a mayúsculas y valida que las placas no estén vacías al insertar
CREATE TRIGGER tg_validar_placas_vehiculo
BEFORE INSERT ON vehiculos
FOR EACH ROW
BEGIN
    SET NEW.placas = UPPER(TRIM(NEW.placas));
    SET NEW.modelo = UPPER(TRIM(NEW.modelo));
    IF NEW.placas = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Placas vacías.';
    END IF;
END$$

-- Normaliza placas y modelo a mayúsculas y valida que las placas no estén vacías al actualizar
CREATE TRIGGER tg_validar_placas_update
BEFORE UPDATE ON vehiculos
FOR EACH ROW
BEGIN
    SET NEW.placas = UPPER(TRIM(NEW.placas));
    SET NEW.modelo = UPPER(TRIM(NEW.modelo));
    IF NEW.placas = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Placas vacías.';
    END IF;
END$$

-- CLIENTES
-- Registra en historial cuando se da de alta un cliente
CREATE TRIGGER tg_auditar_insert_cliente
AFTER INSERT ON clientes
FOR EACH ROW
BEGIN
    INSERT INTO historial (usuario, accion, tabla, detalles)
    VALUES (
        IFNULL(@usuario_actual, USER()),
        'INSERT',
        'CLIENTES',
        CONCAT('Alta cliente ID ', NEW.id_cliente, ': ',
               NEW.nombre, ' ', IFNULL(NEW.apellido_p, ''),
               ' | Tel: ', NEW.telefono_1)
    );
END$$

-- Registra en historial cuando se modifica un cliente
-- y si se desactiva, cancela automáticamente sus vehículos y pólizas
CREATE TRIGGER tg_auditar_update_cliente
AFTER UPDATE ON clientes
FOR EACH ROW
BEGIN
    INSERT INTO historial (usuario, accion, tabla, detalles)
    VALUES (
        IFNULL(@usuario_actual, USER()),
        'UPDATE',
        'CLIENTES',
        CONCAT('Modificación cliente ID ', NEW.id_cliente, ': ',
               NEW.nombre, ' ', IFNULL(NEW.apellido_p, ''),
               ' | Estado: [', OLD.estado, '] → [', NEW.estado, ']')
    );

    -- Cascada automática al desactivar un cliente
    IF OLD.estado = 'Activo' AND NEW.estado = 'Inactivo' THEN
        UPDATE vehiculos
        SET estado = 'Inactivo'
        WHERE id_cliente = NEW.id_cliente;

        UPDATE polizas
        SET estatus = 'Cancelada'
        WHERE id_cliente = NEW.id_cliente AND estatus = 'Activa';
    END IF;
END$$

-- Registra en historial cuando se elimina físicamente un cliente
CREATE TRIGGER tg_auditar_delete_cliente
AFTER DELETE ON clientes
FOR EACH ROW
BEGIN
    INSERT INTO historial (usuario, accion, tabla, detalles)
    VALUES (
        IFNULL(@usuario_actual, USER()),
        'DELETE',
        'CLIENTES',
        CONCAT('Baja cliente ID ', OLD.id_cliente,
               ': ', OLD.nombre, ' ', IFNULL(OLD.apellido_p, ''))
    );
END$$

-- VEHÍCULOS
-- Registra en historial cuando se agrega un vehículo
CREATE TRIGGER tg_auditar_insert_vehiculo
AFTER INSERT ON vehiculos
FOR EACH ROW
BEGIN
    INSERT INTO historial (usuario, accion, tabla, detalles)
    VALUES (
        IFNULL(@usuario_actual, USER()),
        'INSERT',
        'VEHICULOS',
        CONCAT('Alta vehículo ID ', NEW.id_vehiculo,
               ': ', NEW.modelo, ' placas [', NEW.placas, ']',
               ' | Cliente ID: ', NEW.id_cliente)
    );
END$$


-- AUDITORÍA — PÓLIZAS

-- Registra en historial cuando se emite una póliza nueva
CREATE TRIGGER tg_auditar_insert_poliza
AFTER INSERT ON polizas
FOR EACH ROW
BEGIN
    INSERT INTO historial (usuario, accion, tabla, detalles)
    VALUES (
        IFNULL(@usuario_actual, USER()),
        'INSERT',
        'POLIZAS',
        CONCAT('Alta póliza: ', NEW.no_poliza,
               ' | Cliente ID: ', NEW.id_cliente,
               ' | Vehículo ID: ', NEW.id_vehiculo,
               ' | Estatus: ', NEW.estatus)
    );
END$$

-- Registra en historial cambios de estatus en una póliza (cancelación, vencimiento, etc.)
CREATE TRIGGER tg_auditar_update_poliza
AFTER UPDATE ON polizas
FOR EACH ROW
BEGIN
    IF OLD.estatus <> NEW.estatus THEN
        INSERT INTO historial (usuario, accion, tabla, detalles)
        VALUES (
            IFNULL(@usuario_actual, USER()),
            'UPDATE',
            'POLIZAS',
            CONCAT('Cambio póliza: ', NEW.no_poliza,
                   ' | Estatus: [', OLD.estatus, '] → [', NEW.estatus, ']')
        );
    END IF;
END$$

-- PAGOS

-- Registra en historial cada cambio de estatus en un pago de parcialidad
CREATE TRIGGER tg_auditar_cambio_pago
AFTER UPDATE ON pagos_parcialidades
FOR EACH ROW
BEGIN
    IF OLD.estatus <> NEW.estatus THEN
        INSERT INTO historial (usuario, accion, tabla, detalles)
        VALUES (
            IFNULL(@usuario_actual, USER()),
            'UPDATE',
            'PAGOS',
            CONCAT('Pago ID: ', NEW.id_pago,
                   ' | Póliza: ', NEW.no_poliza,
                   ' | Parcialidad ', NEW.num_parcialidad, '/', NEW.total_parcialidades,
                   ' cambió de [', OLD.estatus, '] a [', NEW.estatus, ']')
        );
    END IF;
END$$

DELIMITER ;



DELIMITER $$

-- ─────────────────────────────────────────────────────────────────────────────
-- NIVEL DE AISLAMIENTO: REPEATABLE READ
-- Efecto de lectura que previene: NON-REPEATABLE READ
--   Si dos consultas dentro de la misma transacción leyeran el mismo cliente
--   o póliza, REPEATABLE READ garantiza que el resultado no cambie aunque otra
--   sesión haga UPDATE entre medio. Crítico al registrar cliente + vehículo +
--   póliza + parcialidades: todos los IDs deben referir al mismo estado.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE PROCEDURE registrar_cliente(
    IN p_nombre              VARCHAR(150),
    IN p_apellido_p          VARCHAR(50),
    IN p_apellido_m          VARCHAR(50),
    IN p_telefono_1          VARCHAR(15),
    IN p_correo              VARCHAR(100),
    IN p_modelo              VARCHAR(100),
    IN p_placas              VARCHAR(15),
    IN p_id_compania         INT,
    IN p_id_cobertura        INT,
    IN p_no_poliza           VARCHAR(50),
    IN p_total_parcialidades INT,
    IN p_fecha_inicio        DATE
)
BEGIN
    DECLARE i                   INT  DEFAULT 1;
    DECLARE v_id_cliente        INT;
    DECLARE v_id_vehiculo       INT;
    DECLARE v_fecha_vencimiento DATE;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    INSERT INTO clientes (nombre, apellido_p, apellido_m, telefono_1, correo)
    VALUES (p_nombre, p_apellido_p, p_apellido_m, p_telefono_1, p_correo);
    SET v_id_cliente = LAST_INSERT_ID();

    INSERT INTO vehiculos (id_cliente, modelo, placas)
    VALUES (v_id_cliente, p_modelo, p_placas);
    SET v_id_vehiculo = LAST_INSERT_ID();

    INSERT INTO polizas (no_poliza, id_cliente, id_vehiculo, id_compania, id_cobertura, fecha_inicio, estatus)
    VALUES (p_no_poliza, v_id_cliente, v_id_vehiculo, p_id_compania, p_id_cobertura, p_fecha_inicio, 'Activa');

    WHILE i <= p_total_parcialidades DO
        SET v_fecha_vencimiento = DATE_ADD(p_fecha_inicio, INTERVAL (i - 1) MONTH);
        INSERT INTO pagos_parcialidades (no_poliza, num_parcialidad, total_parcialidades, fecha_vencimiento, estatus)
        VALUES (p_no_poliza, i, p_total_parcialidades, v_fecha_vencimiento, 'Pendiente');
        SET i = i + 1;
    END WHILE;

    COMMIT;
END$$

-- ─────────────────────────────────────────────────────────────────────────────
-- NIVEL DE AISLAMIENTO: READ COMMITTED
-- Efecto de lectura que previene: DIRTY READ
--   Una lectura sucia ocurriría si esta transacción leyera una póliza que otra
--   sesión modificó pero aún no confirmó (COMMIT). Con READ COMMITTED solo se
--   ven datos ya confirmados, evitando cancelar una póliza con datos sucios.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE PROCEDURE cancelar_poliza_segura(
    IN p_no_poliza VARCHAR(50)
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    UPDATE polizas
    SET estatus = 'Cancelada'
    WHERE no_poliza = p_no_poliza;

    UPDATE pagos_parcialidades
    SET estatus = 'Vencido'
    WHERE no_poliza = p_no_poliza AND estatus = 'Pendiente';

    COMMIT;
END$$

-- ─────────────────────────────────────────────────────────────────────────────
-- NIVEL DE AISLAMIENTO: SERIALIZABLE
-- Efecto de lectura que previene: PHANTOM READ
--   Un phantom read ocurriría si durante la renovación otra sesión insertara
--   nuevas parcialidades para la misma póliza. SERIALIZABLE bloquea el rango
--   completo de filas afectadas, garantizando que no aparezcan "filas fantasma"
--   entre la lectura y la escritura de la renovación.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE PROCEDURE renovar_poliza(
    IN p_no_poliza_vieja VARCHAR(50),
    IN p_no_poliza_nueva VARCHAR(50),
    IN p_nueva_fecha     DATE
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    START TRANSACTION;

    UPDATE polizas
    SET no_poliza    = p_no_poliza_nueva,
        fecha_inicio = p_nueva_fecha,
        estatus      = 'Activa'
    WHERE no_poliza = p_no_poliza_vieja;

    COMMIT;
END$$

-- ─────────────────────────────────────────────────────────────────────────────
-- NIVEL DE AISLAMIENTO: READ UNCOMMITTED  (documentado para completar los 4)
-- Efecto de lectura: permite DIRTY READ (lectura de datos no confirmados)
--   Se usa en la baja de cliente porque es una operación interna administrativa
--   donde el rendimiento importa y se acepta el riesgo de leer datos en curso.
--   En producción real se elevaría a READ COMMITTED, pero aquí demuestra el
--   nivel más permisivo de aislamiento.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE PROCEDURE sp_dar_baja_cliente(
    IN p_id_cliente INT
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
    START TRANSACTION;
        INSERT INTO clientes_historico SELECT * FROM clientes WHERE id_cliente = p_id_cliente;
        DELETE FROM clientes WHERE id_cliente = p_id_cliente;
    COMMIT;
END$$

-- Cuenta los vehículos activos de un cliente (útil para validaciones en Java)
CREATE PROCEDURE contar_vehiculos_cliente(
    IN  p_id_cliente      INT,
    OUT p_total_vehiculos INT
)
BEGIN
    SELECT COUNT(*) INTO p_total_vehiculos
    FROM vehiculos
    WHERE id_cliente = p_id_cliente AND estado = 'Activo';
END$$

-- Consulta pólizas que vencen en los próximos 10 días con sus pagos pendientes
CREATE PROCEDURE consultar_vencimientos()
BEGIN
    SELECT
        p.no_poliza,
        CONCAT(c.nombre, ' ', IFNULL(c.apellido_p,''), ' ', IFNULL(c.apellido_m,'')) AS nombre_cliente,
        pp.num_parcialidad,
        pp.fecha_vencimiento
    FROM pagos_parcialidades pp
    JOIN polizas p  ON pp.no_poliza  = p.no_poliza
    JOIN clientes c ON p.id_cliente  = c.id_cliente
    WHERE pp.estatus = 'Pendiente'
      AND pp.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 10 DAY)
    ORDER BY pp.fecha_vencimiento ASC;
END$$

-- Consulta pólizas activas que están por renovar en los próximos 15 días
CREATE PROCEDURE consultar_renovaciones()
BEGIN
    SELECT
        c.id_cliente,
        CONCAT(c.nombre, ' ', IFNULL(c.apellido_p,''), ' ', IFNULL(c.apellido_m,'')) AS cliente,
        c.telefono_1,
        c.correo,
        p.no_poliza,
        co.compania,
        DATE_ADD(p.fecha_inicio, INTERVAL 1 YEAR) AS fecha_expiracion,
        DATEDIFF(DATE_ADD(p.fecha_inicio, INTERVAL 1 YEAR), CURDATE())  AS dias_restantes
    FROM polizas p
    JOIN clientes  c  ON p.id_cliente  = c.id_cliente
    JOIN companias co ON p.id_compania = co.id_compania
    WHERE p.estatus = 'Activa'
      AND DATEDIFF(DATE_ADD(p.fecha_inicio, INTERVAL 1 YEAR), CURDATE()) BETWEEN 0 AND 15
    ORDER BY dias_restantes ASC;
END$$

-- Consulta pagos subsecuentes que vencen entre 4 y 10 días 
CREATE PROCEDURE consultar_pagos_subsecuentes()
BEGIN
    SELECT
        c.id_cliente,
        CONCAT(c.nombre, ' ', IFNULL(c.apellido_p,''), ' ', IFNULL(c.apellido_m,'')) AS cliente,
        c.telefono_1,
        c.correo,
        p.no_poliza,
        pp.num_parcialidad,
        pp.fecha_vencimiento,
        DATEDIFF(pp.fecha_vencimiento, CURDATE()) AS dias_restantes
    FROM pagos_parcialidades pp
    JOIN polizas p  ON pp.no_poliza = p.no_poliza
    JOIN clientes c ON p.id_cliente = c.id_cliente
    WHERE pp.estatus = 'Pendiente'
      AND DATEDIFF(pp.fecha_vencimiento, CURDATE()) BETWEEN 4 AND 10
    ORDER BY dias_restantes ASC;
END$$

-- Consulta pagos urgentes que vencen en 0 a 3 días
CREATE PROCEDURE consultar_alertas_urgentes()
BEGIN
    SELECT
        c.id_cliente,
        CONCAT(c.nombre, ' ', IFNULL(c.apellido_p,''), ' ', IFNULL(c.apellido_m,'')) AS cliente,
        c.telefono_1,
        c.correo,
        p.no_poliza,
        pp.num_parcialidad,
        pp.fecha_vencimiento,
        DATEDIFF(pp.fecha_vencimiento, CURDATE()) AS dias_restantes
    FROM pagos_parcialidades pp
    JOIN polizas p  ON pp.no_poliza = p.no_poliza
    JOIN clientes c ON p.id_cliente = c.id_cliente
    WHERE pp.estatus = 'Pendiente'
      AND DATEDIFF(pp.fecha_vencimiento, CURDATE()) BETWEEN 0 AND 3
    ORDER BY dias_restantes ASC;
END$$

DELIMITER ;


-- ============================================================================
-- SECCIÓN 4 — DATOS INICIALES Y DATOS DE PRUEBA
-- ============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.1  CATÁLOGOS  (mínimo 20 registros entre todas las tablas catálogo)
-- ─────────────────────────────────────────────────────────────────────────────

-- Tipos de cobertura: 8 registros
INSERT IGNORE INTO tipos_cobertura (nombre_cobertura) VALUES
    ('Amplia'),
    ('Limitada'),
    ('Responsabilidad Civil'),
    ('Amplia Plus'),
    ('Básica'),
    ('Daños a Terceros'),
    ('Robo Total'),
    ('Cobertura Médica');

-- Compañías aseguradoras: 12 registros
INSERT IGNORE INTO companias (compania) VALUES
    ('Qualitas'),
    ('GNP'),
    ('AXA'),
    ('Chubb'),
    ('HDI Seguros'),
    ('Mapfre'),
    ('Zurich'),
    ('BBVA Seguros'),
    ('Banorte Seguros'),
    ('Allianz'),
    ('MetLife'),
    ('Insignia Life');

-- Usuarios del sistema: 5 registros
INSERT IGNORE INTO usuarios_sistema (username, password_hash, rol) VALUES
    ('javier',    'Javier123',   'Administrador'),
    ('ulises',    'Ulises123',   'Operador'),
    ('jorge',     'Jorge123',    'Operador'),
    ('karla',     'Karla456',    'Operador'),
    ('mario',     'Mario789',    'Operador');

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.2  CLIENTES  (50 registros — tabla de mayor movimiento)
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO clientes (nombre, apellido_p, apellido_m, telefono_1, telefono_2, correo, estado) VALUES
    ('CARLOS',     'HERNANDEZ', 'GARCIA',    '6671001001', '6671001002', 'carlos.hernandez@gmail.com',  'Activo'),
    ('MARIA',      'LOPEZ',     'TORRES',    '6671002001', NULL,         'maria.lopez@hotmail.com',     'Activo'),
    ('JOSE',       'MARTINEZ',  'RUIZ',      '6671003001', '6671003002', 'jose.martinez@gmail.com',     'Activo'),
    ('ANA',        'RODRIGUEZ', 'PEREZ',     '6671004001', NULL,         'ana.rodriguez@yahoo.com',     'Activo'),
    ('LUIS',       'GONZALEZ',  'FLORES',    '6671005001', '6671005002', 'luis.gonzalez@gmail.com',     'Activo'),
    ('PATRICIA',   'RAMIREZ',   'SANCHEZ',   '6671006001', NULL,         'patricia.ramirez@gmail.com',  'Activo'),
    ('JORGE',      'MORALES',   'JIMENEZ',   '6671007001', '6671007002', 'jorge.morales@hotmail.com',   'Activo'),
    ('LAURA',      'TORRES',    'VARGAS',    '6671008001', NULL,         'laura.torres@gmail.com',      'Activo'),
    ('ROBERTO',    'FLORES',    'MENDEZ',    '6671009001', '6671009002', 'roberto.flores@yahoo.com',    'Activo'),
    ('DIANA',      'CHAVEZ',    'GUERRERO',  '6671010001', NULL,         'diana.chavez@gmail.com',      'Activo'),
    ('PEDRO',      'REYES',     'LUNA',      '6671011001', '6671011002', 'pedro.reyes@gmail.com',       'Activo'),
    ('VERONICA',   'GUTIERREZ', 'CASTILLO',  '6671012001', NULL,         'veronica.gutierrez@gmail.com','Activo'),
    ('FRANCISCO',  'JIMENEZ',   'SOTO',      '6671013001', '6671013002', 'francisco.jimenez@yahoo.com', 'Activo'),
    ('SILVIA',     'MORENO',    'DELGADO',   '6671014001', NULL,         'silvia.moreno@hotmail.com',   'Activo'),
    ('MANUEL',     'RAMOS',     'VEGA',      '6671015001', '6671015002', 'manuel.ramos@gmail.com',      'Activo'),
    ('ALEJANDRA',  'CRUZ',      'RIOS',      '6671016001', NULL,         'alejandra.cruz@gmail.com',    'Activo'),
    ('RICARDO',    'VARGAS',    'MEDINA',    '6671017001', '6671017002', 'ricardo.vargas@yahoo.com',    'Activo'),
    ('GABRIELA',   'MENDOZA',   'ORTIZ',     '6671018001', NULL,         'gabriela.mendoza@gmail.com',  'Activo'),
    ('EDUARDO',    'AGUILAR',   'HERRERA',   '6671019001', '6671019002', 'eduardo.aguilar@hotmail.com', 'Activo'),
    ('ISABEL',     'CASTILLO',  'NAVARRO',   '6671020001', NULL,         'isabel.castillo@gmail.com',   'Activo'),
    ('SERGIO',     'ORTIZ',     'ROMERO',    '6671021001', '6671021002', 'sergio.ortiz@gmail.com',      'Activo'),
    ('TERESA',     'NAVARRO',   'SILVA',     '6671022001', NULL,         'teresa.navarro@yahoo.com',    'Activo'),
    ('HUGO',       'SOTO',      'CAMPOS',    '6671023001', '6671023002', 'hugo.soto@gmail.com',         'Activo'),
    ('CLAUDIA',    'MEDINA',    'ESPINOZA',  '6671024001', NULL,         'claudia.medina@hotmail.com',  'Activo'),
    ('ARTURO',     'HERRERA',   'ACOSTA',    '6671025001', '6671025002', 'arturo.herrera@gmail.com',    'Activo'),
    ('ROSA',       'ROMERO',    'PACHECO',   '6671026001', NULL,         'rosa.romero@gmail.com',       'Activo'),
    ('JAVIER',     'SILVA',     'CORDERO',   '6671027001', '6671027002', 'javier.silva@yahoo.com',      'Activo'),
    ('MARTHA',     'ESPINOZA',  'FUENTES',   '6671028001', NULL,         'martha.espinoza@gmail.com',   'Activo'),
    ('ALFREDO',    'ACOSTA',    'REINA',     '6671029001', '6671029002', 'alfredo.acosta@hotmail.com',  'Activo'),
    ('NORMA',      'PACHECO',   'BERNAL',    '6671030001', NULL,         'norma.pacheco@gmail.com',     'Activo'),
    ('DANIEL',     'FUENTES',   'IBARRA',    '6671031001', '6671031002', 'daniel.fuentes@gmail.com',    'Activo'),
    ('LETICIA',    'CORDERO',   'TREJO',     '6671032001', NULL,         'leticia.cordero@yahoo.com',   'Activo'),
    ('MARTIN',     'BERNAL',    'PONCE',     '6671033001', '6671033002', 'martin.bernal@gmail.com',     'Activo'),
    ('CARMEN',     'IBARRA',    'CABRERA',   '6671034001', NULL,         'carmen.ibarra@hotmail.com',   'Activo'),
    ('ENRIQUE',    'TREJO',     'MONTES',    '6671035001', '6671035002', 'enrique.trejo@gmail.com',     'Activo'),
    ('BEATRIZ',    'PONCE',     'SALINAS',   '6671036001', NULL,         'beatriz.ponce@gmail.com',     'Activo'),
    ('CESAR',      'MONTES',    'ESTRADA',   '6671037001', '6671037002', 'cesar.montes@yahoo.com',      'Activo'),
    ('ADRIANA',    'SALINAS',   'MIRANDA',   '6671038001', NULL,         'adriana.salinas@gmail.com',   'Activo'),
    ('MARIO',      'CABRERA',   'DOMINGUEZ', '6671039001', '6671039002', 'mario.cabrera@hotmail.com',   'Activo'),
    ('ELIZABETH',  'ESTRADA',   'VELASCO',   '6671040001', NULL,         'elizabeth.estrada@gmail.com', 'Activo'),
    ('BENJAMIN',   'MIRANDA',   'TAPIA',     '6671041001', '6671041002', 'benjamin.miranda@gmail.com',  'Activo'),
    ('ESPERANZA',  'DOMINGUEZ', 'DURAN',     '6671042001', NULL,         'esperanza.dominguez@yahoo.com','Activo'),
    ('MARCO',      'VELASCO',   'SOLIS',     '6671043001', '6671043002', 'marco.velasco@gmail.com',     'Activo'),
    ('IMELDA',     'TAPIA',     'QUINONES',  '6671044001', NULL,         'imelda.tapia@hotmail.com',    'Activo'),
    ('GILBERTO',   'DURAN',     'ALVARADO',  '6671045001', '6671045002', 'gilberto.duran@gmail.com',    'Activo'),
    ('NATALIA',    'SOLIS',     'CISNEROS',  '6671046001', NULL,         'natalia.solis@gmail.com',     'Activo'),
    ('OSCAR',      'QUINONES',  'CARDENAS',  '6671047001', '6671047002', 'oscar.quinones@yahoo.com',    'Activo'),
    ('MIRIAM',     'ALVARADO',  'HUERTA',    '6671048001', NULL,         'miriam.alvarado@gmail.com',   'Activo'),
    ('RAUL',       'CISNEROS',  'PALOMINO',  '6671049001', '6671049002', 'raul.cisneros@hotmail.com',   'Activo'),
    ('GUADALUPE',  'CARDENAS',  'ZARATE',    '6671050001', NULL,         'guadalupe.cardenas@gmail.com','Activo');

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.3  VEHÍCULOS  (50 registros — uno por cliente)
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO vehiculos (id_cliente, modelo, placas, estado) VALUES
    (1,  'NISSAN SENTRA 2021',      'ABC-001-A', 'Activo'),
    (2,  'VOLKSWAGEN JETTA 2020',   'ABC-002-B', 'Activo'),
    (3,  'CHEVROLET AVEO 2019',     'ABC-003-C', 'Activo'),
    (4,  'TOYOTA COROLLA 2022',     'ABC-004-D', 'Activo'),
    (5,  'HONDA CIVIC 2021',        'ABC-005-E', 'Activo'),
    (6,  'FORD FIGO 2020',          'ABC-006-F', 'Activo'),
    (7,  'NISSAN KICKS 2022',       'ABC-007-G', 'Activo'),
    (8,  'VOLKSWAGEN POLO 2019',    'ABC-008-H', 'Activo'),
    (9,  'CHEVROLET SPARK 2021',    'ABC-009-I', 'Activo'),
    (10, 'TOYOTA YARIS 2020',       'ABC-010-J', 'Activo'),
    (11, 'HONDA CR-V 2022',         'ABC-011-K', 'Activo'),
    (12, 'FORD RANGER 2021',        'ABC-012-L', 'Activo'),
    (13, 'NISSAN VERSA 2020',       'ABC-013-M', 'Activo'),
    (14, 'VOLKSWAGEN GOLF 2019',    'ABC-014-N', 'Activo'),
    (15, 'CHEVROLET TRAX 2022',     'ABC-015-O', 'Activo'),
    (16, 'TOYOTA HILUX 2021',       'ABC-016-P', 'Activo'),
    (17, 'HONDA HRV 2020',          'ABC-017-Q', 'Activo'),
    (18, 'FORD ECOSPORT 2022',      'ABC-018-R', 'Activo'),
    (19, 'NISSAN MARCH 2019',       'ABC-019-S', 'Activo'),
    (20, 'VOLKSWAGEN VENTO 2021',   'ABC-020-T', 'Activo'),
    (21, 'CHEVROLET BEAT 2020',     'ABC-021-U', 'Activo'),
    (22, 'TOYOTA RAV4 2022',        'ABC-022-V', 'Activo'),
    (23, 'HONDA FIT 2021',          'ABC-023-W', 'Activo'),
    (24, 'FORD TERRITORY 2020',     'ABC-024-X', 'Activo'),
    (25, 'NISSAN FRONTIER 2022',    'ABC-025-Y', 'Activo'),
    (26, 'VOLKSWAGEN TIGUAN 2021',  'ABC-026-Z', 'Activo'),
    (27, 'CHEVROLET EQUINOX 2019',  'BCA-001-A', 'Activo'),
    (28, 'TOYOTA C-HR 2022',        'BCA-002-B', 'Activo'),
    (29, 'HONDA ACCORD 2021',       'BCA-003-C', 'Activo'),
    (30, 'FORD EDGE 2020',          'BCA-004-D', 'Activo'),
    (31, 'NISSAN ALTIMA 2022',      'BCA-005-E', 'Activo'),
    (32, 'VOLKSWAGEN AMAROK 2021',  'BCA-006-F', 'Activo'),
    (33, 'CHEVROLET MALIBU 2019',   'BCA-007-G', 'Activo'),
    (34, 'TOYOTA CAMRY 2022',       'BCA-008-H', 'Activo'),
    (35, 'HONDA PILOT 2021',        'BCA-009-I', 'Activo'),
    (36, 'FORD MUSTANG 2020',       'BCA-010-J', 'Activo'),
    (37, 'NISSAN MURANO 2022',      'BCA-011-K', 'Activo'),
    (38, 'VOLKSWAGEN TOUAREG 2021', 'BCA-012-L', 'Activo'),
    (39, 'CHEVROLET TAHOE 2019',    'BCA-013-M', 'Activo'),
    (40, 'TOYOTA FORTUNER 2022',    'BCA-014-N', 'Activo'),
    (41, 'HONDA ODYSSEY 2021',      'BCA-015-O', 'Activo'),
    (42, 'FORD EXPLORER 2020',      'BCA-016-P', 'Activo'),
    (43, 'NISSAN PATHFINDER 2022',  'BCA-017-Q', 'Activo'),
    (44, 'VOLKSWAGEN PASSAT 2021',  'BCA-018-R', 'Activo'),
    (45, 'CHEVROLET TRAVERSE 2019', 'BCA-019-S', 'Activo'),
    (46, 'TOYOTA PRIUS 2022',       'BCA-020-T', 'Activo'),
    (47, 'HONDA RIDGELINE 2021',    'BCA-021-U', 'Activo'),
    (48, 'FORD BRONCO 2022',        'BCA-022-V', 'Activo'),
    (49, 'NISSAN ARMADA 2021',      'BCA-023-W', 'Activo'),
    (50, 'VOLKSWAGEN CRAFTER 2020', 'BCA-024-X', 'Activo');

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.4  PÓLIZAS  (50 registros)
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO polizas (no_poliza, id_cliente, id_vehiculo, id_compania, id_cobertura, fecha_inicio, cfdi, estatus) VALUES
    ('POL-2024-001', 1,  1,  1, 1, '2024-01-15', TRUE,  'Activa'),
    ('POL-2024-002', 2,  2,  2, 2, '2024-01-20', FALSE, 'Activa'),
    ('POL-2024-003', 3,  3,  3, 3, '2024-02-01', TRUE,  'Activa'),
    ('POL-2024-004', 4,  4,  4, 1, '2024-02-10', FALSE, 'Activa'),
    ('POL-2024-005', 5,  5,  1, 2, '2024-02-15', TRUE,  'Activa'),
    ('POL-2024-006', 6,  6,  2, 3, '2024-03-01', FALSE, 'Cancelada'),
    ('POL-2024-007', 7,  7,  3, 4, '2024-03-05', TRUE,  'Activa'),
    ('POL-2024-008', 8,  8,  4, 5, '2024-03-10', FALSE, 'Activa'),
    ('POL-2024-009', 9,  9,  5, 1, '2024-03-15', TRUE,  'Activa'),
    ('POL-2024-010', 10, 10, 6, 2, '2024-03-20', FALSE, 'Vencida'),
    ('POL-2024-011', 11, 11, 7, 3, '2024-04-01', TRUE,  'Activa'),
    ('POL-2024-012', 12, 12, 8, 4, '2024-04-05', FALSE, 'Activa'),
    ('POL-2024-013', 13, 13, 1, 5, '2024-04-10', TRUE,  'Activa'),
    ('POL-2024-014', 14, 14, 2, 6, '2024-04-15', FALSE, 'Activa'),
    ('POL-2024-015', 15, 15, 3, 7, '2024-04-20', TRUE,  'Cancelada'),
    ('POL-2024-016', 16, 16, 4, 8, '2024-05-01', FALSE, 'Activa'),
    ('POL-2024-017', 17, 17, 5, 1, '2024-05-05', TRUE,  'Activa'),
    ('POL-2024-018', 18, 18, 6, 2, '2024-05-10', FALSE, 'Activa'),
    ('POL-2024-019', 19, 19, 7, 3, '2024-05-15', TRUE,  'Activa'),
    ('POL-2024-020', 20, 20, 8, 4, '2024-05-20', FALSE, 'Vencida'),
    ('POL-2024-021', 21, 21, 1, 5, '2024-06-01', TRUE,  'Activa'),
    ('POL-2024-022', 22, 22, 2, 6, '2024-06-05', FALSE, 'Activa'),
    ('POL-2024-023', 23, 23, 3, 7, '2024-06-10', TRUE,  'Activa'),
    ('POL-2024-024', 24, 24, 4, 8, '2024-06-15', FALSE, 'Activa'),
    ('POL-2024-025', 25, 25, 5, 1, '2024-06-20', TRUE,  'Activa'),
    ('POL-2024-026', 26, 26, 6, 2, '2024-07-01', FALSE, 'Activa'),
    ('POL-2024-027', 27, 27, 7, 3, '2024-07-05', TRUE,  'Cancelada'),
    ('POL-2024-028', 28, 28, 8, 4, '2024-07-10', FALSE, 'Activa'),
    ('POL-2024-029', 29, 29, 1, 5, '2024-07-15', TRUE,  'Activa'),
    ('POL-2024-030', 30, 30, 2, 6, '2024-07-20', FALSE, 'Activa'),
    ('POL-2024-031', 31, 31, 3, 7, '2024-08-01', TRUE,  'Activa'),
    ('POL-2024-032', 32, 32, 4, 8, '2024-08-05', FALSE, 'Activa'),
    ('POL-2024-033', 33, 33, 5, 1, '2024-08-10', TRUE,  'Activa'),
    ('POL-2024-034', 34, 34, 6, 2, '2024-08-15', FALSE, 'Vencida'),
    ('POL-2024-035', 35, 35, 7, 3, '2024-08-20', TRUE,  'Activa'),
    ('POL-2024-036', 36, 36, 8, 4, '2024-09-01', FALSE, 'Activa'),
    ('POL-2024-037', 37, 37, 1, 5, '2024-09-05', TRUE,  'Activa'),
    ('POL-2024-038', 38, 38, 2, 6, '2024-09-10', FALSE, 'Activa'),
    ('POL-2024-039', 39, 39, 3, 7, '2024-09-15', TRUE,  'Cancelada'),
    ('POL-2024-040', 40, 40, 4, 8, '2024-09-20', FALSE, 'Activa'),
    ('POL-2024-041', 41, 41, 5, 1, '2024-10-01', TRUE,  'Activa'),
    ('POL-2024-042', 42, 42, 6, 2, '2024-10-05', FALSE, 'Activa'),
    ('POL-2024-043', 43, 43, 7, 3, '2024-10-10', TRUE,  'Activa'),
    ('POL-2024-044', 44, 44, 8, 4, '2024-10-15', FALSE, 'Activa'),
    ('POL-2024-045', 45, 45, 1, 5, '2024-10-20', TRUE,  'Activa'),
    ('POL-2024-046', 46, 46, 2, 6, '2024-11-01', FALSE, 'Activa'),
    ('POL-2024-047', 47, 47, 3, 7, '2024-11-05', TRUE,  'Activa'),
    ('POL-2024-048', 48, 48, 4, 8, '2024-11-10', FALSE, 'Vencida'),
    ('POL-2024-049', 49, 49, 5, 1, '2024-11-15', TRUE,  'Activa'),
    ('POL-2024-050', 50, 50, 6, 2, '2024-11-20', FALSE, 'Activa');

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.5  PAGOS / PARCIALIDADES  (3 parcialidades por póliza = 150 registros)
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO pagos_parcialidades (no_poliza, num_parcialidad, total_parcialidades, fecha_vencimiento, estatus) VALUES
    ('POL-2024-001',1,3,'2024-01-15','Pagado'), ('POL-2024-001',2,3,'2024-02-15','Pagado'), ('POL-2024-001',3,3,'2024-03-15','Pagado'),
    ('POL-2024-002',1,3,'2024-01-20','Pagado'), ('POL-2024-002',2,3,'2024-02-20','Pagado'), ('POL-2024-002',3,3,'2024-03-20','Pagado'),
    ('POL-2024-003',1,3,'2024-02-01','Pagado'), ('POL-2024-003',2,3,'2024-03-01','Pagado'), ('POL-2024-003',3,3,'2024-04-01','Pendiente'),
    ('POL-2024-004',1,3,'2024-02-10','Pagado'), ('POL-2024-004',2,3,'2024-03-10','Pagado'), ('POL-2024-004',3,3,'2024-04-10','Pendiente'),
    ('POL-2024-005',1,3,'2024-02-15','Pagado'), ('POL-2024-005',2,3,'2024-03-15','Pagado'), ('POL-2024-005',3,3,'2024-04-15','Pendiente'),
    ('POL-2024-006',1,3,'2024-03-01','Pagado'), ('POL-2024-006',2,3,'2024-04-01','Vencido'), ('POL-2024-006',3,3,'2024-05-01','Vencido'),
    ('POL-2024-007',1,3,'2024-03-05','Pagado'), ('POL-2024-007',2,3,'2024-04-05','Pagado'), ('POL-2024-007',3,3,'2024-05-05','Pendiente'),
    ('POL-2024-008',1,3,'2024-03-10','Pagado'), ('POL-2024-008',2,3,'2024-04-10','Pagado'), ('POL-2024-008',3,3,'2024-05-10','Pendiente'),
    ('POL-2024-009',1,3,'2024-03-15','Pagado'), ('POL-2024-009',2,3,'2024-04-15','Pagado'), ('POL-2024-009',3,3,'2024-05-15','Pendiente'),
    ('POL-2024-010',1,3,'2024-03-20','Pagado'), ('POL-2024-010',2,3,'2024-04-20','Vencido'), ('POL-2024-010',3,3,'2024-05-20','Vencido'),
    ('POL-2024-011',1,3,'2024-04-01','Pagado'), ('POL-2024-011',2,3,'2024-05-01','Pagado'), ('POL-2024-011',3,3,'2024-06-01','Pendiente'),
    ('POL-2024-012',1,3,'2024-04-05','Pagado'), ('POL-2024-012',2,3,'2024-05-05','Pagado'), ('POL-2024-012',3,3,'2024-06-05','Pendiente'),
    ('POL-2024-013',1,3,'2024-04-10','Pagado'), ('POL-2024-013',2,3,'2024-05-10','Pendiente'),('POL-2024-013',3,3,'2024-06-10','Pendiente'),
    ('POL-2024-014',1,3,'2024-04-15','Pagado'), ('POL-2024-014',2,3,'2024-05-15','Pendiente'),('POL-2024-014',3,3,'2024-06-15','Pendiente'),
    ('POL-2024-015',1,3,'2024-04-20','Pagado'), ('POL-2024-015',2,3,'2024-05-20','Vencido'), ('POL-2024-015',3,3,'2024-06-20','Vencido'),
    ('POL-2024-016',1,3,'2024-05-01','Pagado'), ('POL-2024-016',2,3,'2024-06-01','Pagado'), ('POL-2024-016',3,3,'2024-07-01','Pendiente'),
    ('POL-2024-017',1,3,'2024-05-05','Pagado'), ('POL-2024-017',2,3,'2024-06-05','Pagado'), ('POL-2024-017',3,3,'2024-07-05','Pendiente'),
    ('POL-2024-018',1,3,'2024-05-10','Pagado'), ('POL-2024-018',2,3,'2024-06-10','Pagado'), ('POL-2024-018',3,3,'2024-07-10','Pendiente'),
    ('POL-2024-019',1,3,'2024-05-15','Pagado'), ('POL-2024-019',2,3,'2024-06-15','Pendiente'),('POL-2024-019',3,3,'2024-07-15','Pendiente'),
    ('POL-2024-020',1,3,'2024-05-20','Pagado'), ('POL-2024-020',2,3,'2024-06-20','Vencido'), ('POL-2024-020',3,3,'2024-07-20','Vencido'),
    ('POL-2024-021',1,3,'2024-06-01','Pagado'), ('POL-2024-021',2,3,'2024-07-01','Pagado'), ('POL-2024-021',3,3,'2024-08-01','Pendiente'),
    ('POL-2024-022',1,3,'2024-06-05','Pagado'), ('POL-2024-022',2,3,'2024-07-05','Pagado'), ('POL-2024-022',3,3,'2024-08-05','Pendiente'),
    ('POL-2024-023',1,3,'2024-06-10','Pagado'), ('POL-2024-023',2,3,'2024-07-10','Pendiente'),('POL-2024-023',3,3,'2024-08-10','Pendiente'),
    ('POL-2024-024',1,3,'2024-06-15','Pagado'), ('POL-2024-024',2,3,'2024-07-15','Pendiente'),('POL-2024-024',3,3,'2024-08-15','Pendiente'),
    ('POL-2024-025',1,3,'2024-06-20','Pagado'), ('POL-2024-025',2,3,'2024-07-20','Pagado'), ('POL-2024-025',3,3,'2024-08-20','Pendiente'),
    ('POL-2024-026',1,3,'2024-07-01','Pagado'), ('POL-2024-026',2,3,'2024-08-01','Pendiente'),('POL-2024-026',3,3,'2024-09-01','Pendiente'),
    ('POL-2024-027',1,3,'2024-07-05','Pagado'), ('POL-2024-027',2,3,'2024-08-05','Vencido'), ('POL-2024-027',3,3,'2024-09-05','Vencido'),
    ('POL-2024-028',1,3,'2024-07-10','Pagado'), ('POL-2024-028',2,3,'2024-08-10','Pendiente'),('POL-2024-028',3,3,'2024-09-10','Pendiente'),
    ('POL-2024-029',1,3,'2024-07-15','Pagado'), ('POL-2024-029',2,3,'2024-08-15','Pendiente'),('POL-2024-029',3,3,'2024-09-15','Pendiente'),
    ('POL-2024-030',1,3,'2024-07-20','Pagado'), ('POL-2024-030',2,3,'2024-08-20','Pagado'), ('POL-2024-030',3,3,'2024-09-20','Pendiente'),
    ('POL-2024-031',1,3,'2024-08-01','Pagado'), ('POL-2024-031',2,3,'2024-09-01','Pendiente'),('POL-2024-031',3,3,'2024-10-01','Pendiente'),
    ('POL-2024-032',1,3,'2024-08-05','Pagado'), ('POL-2024-032',2,3,'2024-09-05','Pendiente'),('POL-2024-032',3,3,'2024-10-05','Pendiente'),
    ('POL-2024-033',1,3,'2024-08-10','Pagado'), ('POL-2024-033',2,3,'2024-09-10','Pagado'), ('POL-2024-033',3,3,'2024-10-10','Pendiente'),
    ('POL-2024-034',1,3,'2024-08-15','Pagado'), ('POL-2024-034',2,3,'2024-09-15','Vencido'), ('POL-2024-034',3,3,'2024-10-15','Vencido'),
    ('POL-2024-035',1,3,'2024-08-20','Pagado'), ('POL-2024-035',2,3,'2024-09-20','Pendiente'),('POL-2024-035',3,3,'2024-10-20','Pendiente'),
    ('POL-2024-036',1,3,'2024-09-01','Pagado'), ('POL-2024-036',2,3,'2024-10-01','Pendiente'),('POL-2024-036',3,3,'2024-11-01','Pendiente'),
    ('POL-2024-037',1,3,'2024-09-05','Pagado'), ('POL-2024-037',2,3,'2024-10-05','Pagado'), ('POL-2024-037',3,3,'2024-11-05','Pendiente'),
    ('POL-2024-038',1,3,'2024-09-10','Pagado'), ('POL-2024-038',2,3,'2024-10-10','Pendiente'),('POL-2024-038',3,3,'2024-11-10','Pendiente'),
    ('POL-2024-039',1,3,'2024-09-15','Pagado'), ('POL-2024-039',2,3,'2024-10-15','Vencido'), ('POL-2024-039',3,3,'2024-11-15','Vencido'),
    ('POL-2024-040',1,3,'2024-09-20','Pagado'), ('POL-2024-040',2,3,'2024-10-20','Pendiente'),('POL-2024-040',3,3,'2024-11-20','Pendiente'),
    ('POL-2024-041',1,3,'2024-10-01','Pagado'), ('POL-2024-041',2,3,'2024-11-01','Pendiente'),('POL-2024-041',3,3,'2024-12-01','Pendiente'),
    ('POL-2024-042',1,3,'2024-10-05','Pagado'), ('POL-2024-042',2,3,'2024-11-05','Pendiente'),('POL-2024-042',3,3,'2024-12-05','Pendiente'),
    ('POL-2024-043',1,3,'2024-10-10','Pagado'), ('POL-2024-043',2,3,'2024-11-10','Pagado'), ('POL-2024-043',3,3,'2024-12-10','Pendiente'),
    ('POL-2024-044',1,3,'2024-10-15','Pagado'), ('POL-2024-044',2,3,'2024-11-15','Pendiente'),('POL-2024-044',3,3,'2024-12-15','Pendiente'),
    ('POL-2024-045',1,3,'2024-10-20','Pagado'), ('POL-2024-045',2,3,'2024-11-20','Pendiente'),('POL-2024-045',3,3,'2024-12-20','Pendiente'),
    ('POL-2024-046',1,3,'2024-11-01','Pagado'), ('POL-2024-046',2,3,'2024-12-01','Pendiente'),('POL-2024-046',3,3,'2025-01-01','Pendiente'),
    ('POL-2024-047',1,3,'2024-11-05','Pagado'), ('POL-2024-047',2,3,'2024-12-05','Pendiente'),('POL-2024-047',3,3,'2025-01-05','Pendiente'),
    ('POL-2024-048',1,3,'2024-11-10','Pagado'), ('POL-2024-048',2,3,'2024-12-10','Vencido'), ('POL-2024-048',3,3,'2025-01-10','Vencido'),
    ('POL-2024-049',1,3,'2024-11-15','Pagado'), ('POL-2024-049',2,3,'2024-12-15','Pendiente'),('POL-2024-049',3,3,'2025-01-15','Pendiente'),
    ('POL-2024-050',1,3,'2024-11-20','Pagado'), ('POL-2024-050',2,3,'2024-12-20','Pendiente'),('POL-2024-050',3,3,'2025-01-20','Pendiente');

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.6  INSTRUCCIONES UPDATE  (mínimo 5 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- U1. Actualizar el teléfono de un cliente que reportó cambio de número
UPDATE clientes
SET telefono_1 = '6672001001', telefono_2 = '6672001002'
WHERE id_cliente = 5;

-- U2. Corregir el correo electrónico de un cliente
UPDATE clientes
SET correo = 'luis.gonzalez.nuevo@outlook.com'
WHERE id_cliente = 5;

-- U3. Marcar como Inactivo a un cliente que pidió baja temporal del servicio
UPDATE clientes
SET estado = 'Inactivo'
WHERE id_cliente = 10;

-- U4. Cambiar la cobertura de una póliza activa a una más amplia (endoso)
UPDATE polizas
SET id_cobertura = 1
WHERE no_poliza = 'POL-2024-003' AND estatus = 'Activa';

-- U5. Actualizar el estatus del segundo pago de una póliza al recibir el depósito
UPDATE pagos_parcialidades
SET estatus = 'Pagado'
WHERE no_poliza = 'POL-2024-013' AND num_parcialidad = 2;

-- U6. Activar nuevamente a un cliente que había quedado inactivo por error
UPDATE clientes
SET estado = 'Activo'
WHERE id_cliente = 10;

-- U7. Actualizar la compañía aseguradora de una póliza por cambio de proveedor
UPDATE polizas
SET id_compania = 5
WHERE no_poliza = 'POL-2024-008' AND estatus = 'Activa';

-- ─────────────────────────────────────────────────────────────────────────────
-- 4.7  INSTRUCCIONES DELETE  (mínimo 5 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- D1. Eliminar un pago duplicado que fue registrado por error
DELETE FROM pagos_parcialidades
WHERE no_poliza = 'POL-2024-050' AND num_parcialidad = 3 AND estatus = 'Pendiente';

-- D2. Eliminar un vehículo dado de alta por error (sin póliza asignada)
-- Primero insertamos un vehículo de prueba para poder eliminarlo
INSERT INTO vehiculos (id_cliente, modelo, placas) VALUES (1, 'PRUEBA ELIMINACION', 'DEL-000-X');
DELETE FROM vehiculos WHERE placas = 'DEL-000-X';

-- D3. Eliminar registros de historial de auditoría más antiguos de cierta fecha (limpieza)
DELETE FROM historial
WHERE fecha < '2023-01-01';

-- D4. Eliminar un usuario del sistema que ya no labora en la empresa
INSERT INTO usuarios_sistema (username, password_hash, rol) VALUES ('empleado_baja', 'Temp123', 'Operador');
DELETE FROM usuarios_sistema WHERE username = 'empleado_baja';

-- D5. Limpiar los pagos vencidos de pólizas que ya están canceladas
DELETE FROM pagos_parcialidades
WHERE estatus = 'Vencido'
  AND no_poliza IN (SELECT no_poliza FROM polizas WHERE estatus = 'Cancelada')
  AND num_parcialidad > 1;




-- Administrador: control total
CREATE USER IF NOT EXISTS 'javier'@'%' IDENTIFIED BY 'Javier123';
GRANT ALL PRIVILEGES ON segurosmurillo.* TO 'javier'@'%' WITH GRANT OPTION;

CREATE USER IF NOT EXISTS 'ulises'@'%' IDENTIFIED BY 'Ulises123';
GRANT SELECT, INSERT, UPDATE ON segurosmurillo.clientes TO 'ulises'@'%';
GRANT SELECT, INSERT, UPDATE ON segurosmurillo.vehiculos TO 'ulises'@'%';
GRANT SELECT, INSERT, UPDATE ON segurosmurillo.polizas TO 'ulises'@'%';
GRANT SELECT ON segurosmurillo.tipos_cobertura TO 'ulises'@'%';
GRANT SELECT ON segurosmurillo.companias TO 'ulises'@'%';
GRANT SELECT ON segurosmurillo.usuarios_sistema TO 'ulises'@'%';
GRANT INSERT, SELECT ON segurosmurillo.historial TO 'ulises'@'%';
GRANT EXECUTE ON PROCEDURE segurosmurillo.contar_vehiculos_cliente TO 'ulises'@'%';
GRANT EXECUTE ON PROCEDURE segurosmurillo.renovar_poliza TO 'ulises'@'%';
GRANT EXECUTE ON PROCEDURE segurosmurillo.cancelar_poliza_segura TO 'ulises'@'%';

-- solo lectura y reportes
CREATE USER IF NOT EXISTS 'jorge'@'%' IDENTIFIED BY 'Jorge123';
GRANT SELECT ON segurosmurillo.historial  TO 'jorge'@'%';
GRANT SELECT ON segurosmurillo.clientes   TO 'jorge'@'%';
GRANT SELECT ON segurosmurillo.vehiculos  TO 'jorge'@'%';
GRANT SELECT ON segurosmurillo.polizas    TO 'jorge'@'%';
GRANT EXECUTE ON PROCEDURE segurosmurillo.consultar_renovaciones    TO 'jorge'@'%';
GRANT EXECUTE ON PROCEDURE segurosmurillo.consultar_alertas_urgentes TO 'jorge'@'%';

-- Recepción
CREATE USER IF NOT EXISTS 'recepcion_seguros'@'%' IDENTIFIED BY 'Consulta987';
GRANT SELECT ON segurosmurillo.clientes TO 'recepcion_seguros'@'%';
GRANT SELECT ON segurosmurillo.vehiculos TO 'recepcion_seguros'@'%';
GRANT SELECT ON segurosmurillo.polizas TO 'recepcion_seguros'@'%';
GRANT SELECT ON segurosmurillo.tipos_cobertura TO 'recepcion_seguros'@'%';
GRANT SELECT ON segurosmurillo.companias TO 'recepcion_seguros'@'%';

FLUSH PRIVILEGES;


-- ============================================================================
-- SECCIÓN 6 — UNIDAD II: CONSULTAS DE VALIDACIÓN DEL DISEÑO
-- ============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- 6.1  CONSULTAS CON JOIN (8 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- J1. Pólizas con el nombre del asegurado y modelo del vehículo
SELECT p.no_poliza,
       CONCAT(c.nombre, ' ', c.apellido_p)   AS asegurado,
       v.modelo                               AS automovil
FROM polizas p
INNER JOIN clientes  c ON p.id_cliente  = c.id_cliente
INNER JOIN vehiculos v ON p.id_vehiculo = v.id_vehiculo;

-- J2. Compañía aseguradora que atiende a cada cliente
SELECT DISTINCT CONCAT(c.nombre, ' ', c.apellido_p) AS cliente,
                comp.compania                        AS aseguradora
FROM clientes c
INNER JOIN polizas   p    ON c.id_cliente   = p.id_cliente
INNER JOIN companias comp ON p.id_compania  = comp.id_compania;

-- J3. Pagos pendientes con datos de contacto del cliente para cobranza
SELECT pp.id_pago,
       pp.no_poliza,
       CONCAT(c.nombre, ' ', c.apellido_p) AS cliente,
       c.telefono_1,
       pp.fecha_vencimiento
FROM pagos_parcialidades pp
INNER JOIN polizas  p ON pp.no_poliza  = p.no_poliza
INNER JOIN clientes c ON p.id_cliente  = c.id_cliente
WHERE pp.estatus = 'Pendiente';

-- J4. Todos los vehículos registrados y su propietario
SELECT v.id_vehiculo,
       v.modelo,
       v.placas,
       CONCAT(c.nombre, ' ', c.apellido_p) AS propietario
FROM vehiculos v
INNER JOIN clientes c ON v.id_cliente = c.id_cliente;

-- J5. Pólizas cruzadas con su tipo de cobertura contratada
SELECT p.no_poliza,
       tc.nombre_cobertura AS cobertura_contratada,
       p.fecha_inicio,
       p.estatus
FROM polizas p
INNER JOIN tipos_cobertura tc ON p.id_cobertura = tc.id_cobertura;

-- J6. Reporte maestro: cliente, vehículo, aseguradora y cobertura en una sola consulta
SELECT p.no_poliza,
       c.nombre                AS cliente,
       v.modelo                AS auto,
       comp.compania           AS aseguradora,
       tc.nombre_cobertura     AS cobertura
FROM polizas p
INNER JOIN clientes        c    ON p.id_cliente   = c.id_cliente
INNER JOIN vehiculos       v    ON p.id_vehiculo  = v.id_vehiculo
INNER JOIN companias       comp ON p.id_compania  = comp.id_compania
INNER JOIN tipos_cobertura tc   ON p.id_cobertura = tc.id_cobertura;

-- J7. LEFT JOIN: vehículos registrados sin póliza asignada
SELECT v.id_vehiculo, v.modelo, v.placas
FROM vehiculos v
LEFT JOIN polizas p ON v.id_vehiculo = p.id_vehiculo
WHERE p.no_poliza IS NULL;

-- J8. Total de pólizas por aseguradora (incluye las que tienen cero pólizas)
SELECT comp.compania,
       COUNT(p.no_poliza) AS total_polizas
FROM companias comp
LEFT JOIN polizas p ON comp.id_compania = p.id_compania
GROUP BY comp.id_compania, comp.compania;


-- ─────────────────────────────────────────────────────────────────────────────
-- 6.2  CONSULTAS CON OPERADOR SET — UNION (2 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- S1. Lista unificada de clientes activos y clientes dados de baja
SELECT id_cliente, nombre, apellido_p, 'Activo'    AS tipo
FROM clientes
UNION
SELECT id_cliente, nombre, apellido_p, 'Dado de baja' AS tipo
FROM clientes_historico;

-- S2. Todos los números de póliza que tienen algún problema:
--     canceladas o con pagos vencidos
SELECT no_poliza, 'Cancelada' AS situacion
FROM polizas
WHERE estatus = 'Cancelada'
UNION
SELECT no_poliza, 'Pago vencido' AS situacion
FROM pagos_parcialidades
WHERE estatus = 'Vencido';


-- ─────────────────────────────────────────────────────────────────────────────
-- 6.3  SUBCONSULTAS (4 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- SC1. Pólizas de clientes que tienen correo de Gmail
SELECT no_poliza, fecha_inicio, estatus
FROM polizas
WHERE id_cliente IN (
    SELECT id_cliente
    FROM clientes
    WHERE correo LIKE '%@gmail.com'
);

-- SC2. Vehículos de clientes cuyo apellido empieza con la letra 'G'
SELECT id_vehiculo, modelo, placas
FROM vehiculos
WHERE id_cliente IN (
    SELECT id_cliente
    FROM clientes
    WHERE apellido_p LIKE 'G%'
);

-- SC3. Registros del historial hechos por el administrador del sistema
SELECT id_log, usuario, accion, detalles, fecha
FROM historial
WHERE usuario IN (
    SELECT username
    FROM usuarios_sistema
    WHERE rol = 'Administrador'
);

-- SC4. Pólizas de vehículos que sean marca Nissan
SELECT no_poliza, id_compania, estatus
FROM polizas
WHERE id_vehiculo IN (
    SELECT id_vehiculo
    FROM vehiculos
    WHERE modelo LIKE 'NISSAN%'
);


-- ─────────────────────────────────────────────────────────────────────────────
-- 6.4  CONSULTAS CON GROUP BY + HAVING (4 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- H1. Cuántos vehículos tiene registrados cada cliente
SELECT id_cliente,
       COUNT(id_vehiculo) AS total_vehiculos
FROM vehiculos
GROUP BY id_cliente
HAVING COUNT(id_vehiculo) >= 1;

-- H2. Compañías con más de 3 pólizas contratadas
SELECT id_compania,
       COUNT(no_poliza) AS total_polizas
FROM polizas
GROUP BY id_compania
HAVING COUNT(no_poliza) > 3;

-- H3. Pólizas que tienen más de 1 pago registrado
SELECT no_poliza,
       COUNT(id_pago)   AS total_pagos,
       SUM(CASE WHEN estatus = 'Pagado' THEN 1 ELSE 0 END) AS pagados
FROM pagos_parcialidades
GROUP BY no_poliza
HAVING COUNT(id_pago) > 1;

-- H4. Acciones del historial que se repitieron más de 5 veces
SELECT accion,
       tabla,
       COUNT(id_log) AS veces
FROM historial
GROUP BY accion, tabla
HAVING COUNT(id_log) > 5;


-- ─────────────────────────────────────────────────────────────────────────────
-- 6.5  VISTAS (4 requeridas)
-- ─────────────────────────────────────────────────────────────────────────────

-- V1. Pólizas activas con nombre del cliente y compañía aseguradora
CREATE OR REPLACE VIEW vista_polizas_vigentes AS
SELECT p.no_poliza,
       CONCAT(c.nombre, ' ', c.apellido_p) AS cliente,
       v.modelo                             AS vehiculo,
       comp.compania,
       p.fecha_inicio
FROM polizas p
JOIN clientes  c    ON p.id_cliente  = c.id_cliente
JOIN vehiculos v    ON p.id_vehiculo = v.id_vehiculo
JOIN companias comp ON p.id_compania = comp.id_compania
WHERE p.estatus = 'Activa';

-- V2. Pagos que están pendientes o vencidos (cartera de cobranza)
CREATE OR REPLACE VIEW vista_cartera_vencida AS
SELECT id_pago,
       no_poliza,
       num_parcialidad,
       fecha_vencimiento,
       estatus
FROM pagos_parcialidades
WHERE estatus = 'Pendiente' OR estatus = 'Vencido';

-- V3. Resumen de clientes: nombre, teléfono y cuántos vehículos tienen
CREATE OR REPLACE VIEW vista_clientes_vehiculos AS
SELECT c.id_cliente,
       CONCAT(c.nombre, ' ', c.apellido_p) AS cliente,
       c.telefono_1,
       COUNT(v.id_vehiculo) AS total_vehiculos
FROM clientes c
JOIN vehiculos v ON c.id_cliente = v.id_cliente
GROUP BY c.id_cliente, c.nombre, c.apellido_p, c.telefono_1;

-- V4. Historial de eliminaciones y modificaciones de clientes
CREATE OR REPLACE VIEW vista_auditoria_clientes AS
SELECT id_log,
       usuario,
       accion,
       fecha,
       detalles
FROM historial
WHERE tabla = 'CLIENTES';
