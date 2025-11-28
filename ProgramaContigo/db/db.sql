CREATE DATABASE IF NOT EXISTS program_contigo;
USE program_contigo;

-- Tabla de beneficiarios
CREATE TABLE beneficiarios (
                               dni VARCHAR(8) PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL,
                               apellido VARCHAR(100) NOT NULL,
                               tiene_certificado_discapacidad BOOLEAN NOT NULL DEFAULT FALSE,
                               tiene_otros_ingresos BOOLEAN NOT NULL DEFAULT FALSE,
                               clasificacion_economica ENUM('pobre extremo', 'pobre', 'no pobre') NOT NULL,
                               region VARCHAR(100) NOT NULL,
                               pension_recibida DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                               fecha_inscripcion DATE NOT NULL,
                               padron_1_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               padron_2_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               padron_3_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               padron_4_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               padron_5_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               padron_6_cobrado BOOLEAN NOT NULL DEFAULT FALSE,
                               fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SELECT * FROM beneficiarios;

-- Tabla de autorizados
CREATE TABLE autorizados (
                             dni VARCHAR(8) PRIMARY KEY,
                             nombre VARCHAR(100) NOT NULL,
                             apellido VARCHAR(100) NOT NULL,
                             dni_beneficiario VARCHAR(8) NOT NULL,
                             parentesco VARCHAR(50) NOT NULL,
                             autorizado BOOLEAN NOT NULL DEFAULT TRUE,
                             fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (dni_beneficiario) REFERENCES beneficiarios(dni) ON DELETE CASCADE
);

SELECT * FROM autorizados;


-- Tabla de certificados médicos
CREATE TABLE certificados (
                              codigo VARCHAR(20) PRIMARY KEY,
                              dni_paciente VARCHAR(8) NOT NULL,
                              grado_discapacidad ENUM('severa', 'moderada', 'leve') NOT NULL,
                              vigente BOOLEAN NOT NULL DEFAULT TRUE,
                              fecha_emision DATE NOT NULL DEFAULT (CURRENT_DATE),
                              fecha_vencimiento DATE,
                              fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (dni_paciente) REFERENCES beneficiarios(dni) ON DELETE CASCADE
);

SELECT * FROM certificados;

-- Tabla de padrones (para registro histórico)
CREATE TABLE padrones (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          numero_padron INT NOT NULL,
                          periodo_meses VARCHAR(50) NOT NULL,
                          fecha_pago DATE NOT NULL,
                          monto DECIMAL(10,2) NOT NULL DEFAULT 300.00,
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT * FROM padrones;

-- Tabla de pagos realizados (historial de transacciones)
CREATE TABLE pagos_realizados (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  dni_beneficiario VARCHAR(8) NOT NULL,
                                  numero_padron INT NOT NULL,
                                  monto DECIMAL(10,2) NOT NULL,
                                  fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  estado ENUM('procesado', 'pendiente', 'anulado') DEFAULT 'procesado',
                                  observaciones TEXT,
                                  FOREIGN KEY (dni_beneficiario) REFERENCES beneficiarios(dni) ON DELETE CASCADE,
                                  UNIQUE KEY uq_beneficiario_padron (dni_beneficiario, numero_padron)
);

ALTER TABLE padrones
    ADD CONSTRAINT uk_numero_padron UNIQUE (numero_padron);

ALTER TABLE pagos_realizados
    ADD CONSTRAINT fk_pagos_padron
        FOREIGN KEY (numero_padron) REFERENCES padrones(numero_padron) ON DELETE RESTRICT;

SELECT * FROM pagos_realizados;

-- Insertar padrones del año 2025
INSERT INTO padrones (numero_padron, periodo_meses, fecha_pago, monto) VALUES
                                                                           (1, 'enero-febrero', '2025-02-21', 300.00),
                                                                           (2, 'marzo-abril', '2025-04-25', 300.00),
                                                                           (3, 'mayo-junio', '2025-06-20', 300.00),
                                                                           (4, 'julio-agosto', '2025-08-22', 300.00),
                                                                           (5, 'septiembre-octubre', '2025-10-24', 300.00),
                                                                           (6, 'noviembre-diciembre', '2025-12-05', 300.00);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_beneficiarios_region ON beneficiarios(region);
CREATE INDEX idx_beneficiarios_clasificacion ON beneficiarios(clasificacion_economica);
CREATE INDEX idx_beneficiarios_fecha_inscripcion ON beneficiarios(fecha_inscripcion);
CREATE INDEX idx_autorizados_beneficiario ON autorizados(dni_beneficiario);
CREATE INDEX idx_certificados_paciente ON certificados(dni_paciente);
CREATE INDEX idx_pagos_beneficiario ON pagos_realizados(dni_beneficiario);
CREATE INDEX idx_pagos_fecha ON pagos_realizados(fecha_pago);

-- Vistas útiles para reportes
CREATE VIEW v_beneficiarios_elegibles AS
SELECT
    b.*,
    c.grado_discapacidad,
    c.vigente as certificado_vigente
FROM beneficiarios b
         LEFT JOIN certificados c ON b.dni = c.dni_paciente
WHERE b.tiene_certificado_discapacidad = TRUE
  AND b.tiene_otros_ingresos = FALSE
  AND b.clasificacion_economica IN ('pobre', 'pobre extremo')
  AND (c.vigente = TRUE AND c.grado_discapacidad = 'severa' OR c.codigo IS NULL);

SELECT * FROM v_beneficiarios_elegibles;

CREATE VIEW v_resumen_pagos AS
SELECT
    b.dni,
    b.nombre,
    b.apellido,
    b.pension_recibida,
    COUNT(p.id) as total_pagos_realizados,
    SUM(p.monto) as monto_total_pagado
FROM beneficiarios b
         LEFT JOIN pagos_realizados p ON b.dni = p.dni_beneficiario
GROUP BY b.dni, b.nombre, b.apellido, b.pension_recibida;

SELECT * FROM v_resumen_pagos;

-- Procedimientos almacenados útiles
DELIMITER //

CREATE PROCEDURE sp_verificar_elegibilidad(IN p_dni VARCHAR(8))
BEGIN
SELECT
    b.dni,
    b.nombre,
    b.apellido,
    b.tiene_certificado_discapacidad,
    b.tiene_otros_ingresos,
    b.clasificacion_economica,
    CASE
        WHEN c.vigente = TRUE AND c.grado_discapacidad = 'severa'
            AND b.tiene_otros_ingresos = FALSE
            AND b.clasificacion_economica IN ('pobre', 'pobre extremo')
            THEN 'ELEGIBLE'
        ELSE 'NO ELEGIBLE'
        END as estado_elegibilidad
FROM beneficiarios b
         LEFT JOIN certificados c ON b.dni = c.dni_paciente
WHERE b.dni = p_dni;
END //

DELIMITER //
CREATE PROCEDURE sp_procesar_pago(
    IN p_dni VARCHAR(8),
    IN p_numero_padron INT,
    OUT p_resultado VARCHAR(100)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_elegible BOOLEAN DEFAULT FALSE;

    -- Verificar si ya cobró este padrón
SELECT COUNT(*) INTO v_count
FROM pagos_realizados
WHERE dni_beneficiario = p_dni AND numero_padron = p_numero_padron;

IF v_count > 0 THEN
        SET p_resultado = 'ERROR: Padrón ya cobrado';
ELSE
        -- Verificar elegibilidad
SELECT COUNT(*) INTO v_count
FROM v_beneficiarios_elegibles
WHERE dni = p_dni;

IF v_count > 0 THEN
            -- Procesar pago
            INSERT INTO pagos_realizados (dni_beneficiario, numero_padron, monto)
            VALUES (p_dni, p_numero_padron, 300.00);

            -- Actualizar beneficiario
UPDATE beneficiarios
SET pension_recibida = pension_recibida + 300.00,
    padron_1_cobrado = CASE WHEN p_numero_padron = 1 THEN TRUE ELSE padron_1_cobrado END,
    padron_2_cobrado = CASE WHEN p_numero_padron = 2 THEN TRUE ELSE padron_2_cobrado END,
    padron_3_cobrado = CASE WHEN p_numero_padron = 3 THEN TRUE ELSE padron_3_cobrado END,
    padron_4_cobrado = CASE WHEN p_numero_padron = 4 THEN TRUE ELSE padron_4_cobrado END,
    padron_5_cobrado = CASE WHEN p_numero_padron = 5 THEN TRUE ELSE padron_5_cobrado END,
    padron_6_cobrado = CASE WHEN p_numero_padron = 6 THEN TRUE ELSE padron_6_cobrado END
WHERE dni = p_dni;

SET p_resultado = 'PAGO PROCESADO EXITOSAMENTE';
ELSE
            SET p_resultado = 'ERROR: Beneficiario no elegible';
END IF;
END IF;
END //
DELIMITER ;

DELIMITER ;

-- Tabla para auditoría
CREATE TABLE auditoria_beneficiarios (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         dni VARCHAR(8),
                                         accion ENUM('INSERT', 'UPDATE', 'DELETE'),
                                         campo_modificado VARCHAR(50),
                                         valor_anterior TEXT,
                                         valor_nuevo TEXT,
                                         usuario VARCHAR(50),
                                         fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE auditoria_beneficiarios
    ADD CONSTRAINT fk_auditoria_beneficiario
        FOREIGN KEY (dni) REFERENCES beneficiarios(dni) ON DELETE CASCADE;

SELECT * FROM auditoria_beneficiarios;


-- Triggers para auditoría
DELIMITER //

CREATE TRIGGER tr_beneficiarios_update
    AFTER UPDATE ON beneficiarios
    FOR EACH ROW
BEGIN
    IF OLD.clasificacion_economica != NEW.clasificacion_economica THEN
        INSERT INTO auditoria_beneficiarios (dni, accion, campo_modificado, valor_anterior, valor_nuevo)
        VALUES (NEW.dni, 'UPDATE', 'clasificacion_economica', OLD.clasificacion_economica, NEW.clasificacion_economica);
END IF;

IF OLD.tiene_otros_ingresos != NEW.tiene_otros_ingresos THEN
        INSERT INTO auditoria_beneficiarios (dni, accion, campo_modificado, valor_anterior, valor_nuevo)
        VALUES (NEW.dni, 'UPDATE', 'tiene_otros_ingresos', OLD.tiene_otros_ingresos, NEW.tiene_otros_ingresos);
END IF;

    IF OLD.region != NEW.region THEN
        INSERT INTO auditoria_beneficiarios (dni, accion, campo_modificado, valor_anterior, valor_nuevo)
        VALUES (NEW.dni, 'UPDATE', 'region', OLD.region, NEW.region);
END IF;
END //

DELIMITER ;