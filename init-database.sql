-- ============================================
-- CoopCredit - Script SQL Completo para PostgreSQL
-- ============================================
-- Este script crea el esquema completo de la base de datos
-- y los datos iniciales de prueba.
-- 
-- Ejecutar en PostgreSQL después de crear la base de datos:
-- createdb coopcredit
-- psql -d coopcredit -f init-database.sql
-- ============================================

-- ============================================
-- PARTE 1: CREACIÓN DEL ESQUEMA
-- ============================================

-- Tabla: afiliados
CREATE TABLE IF NOT EXISTS afiliados (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(15) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    salario DECIMAL(15, 2) NOT NULL CHECK (salario > 0),
    fecha_afiliacion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_documento CHECK (LENGTH(documento) >= 6)
);

-- Índices para afiliados
CREATE INDEX IF NOT EXISTS idx_afiliados_documento ON afiliados(documento);
CREATE INDEX IF NOT EXISTS idx_afiliados_estado ON afiliados(estado);

-- Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    afiliado_id BIGINT,
    CONSTRAINT fk_usuarios_afiliado FOREIGN KEY (afiliado_id) REFERENCES afiliados(id) ON DELETE SET NULL
);

-- Índices para usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- Tabla: usuarios_roles
CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ROLE_AFILIADO', 'ROLE_ANALISTA', 'ROLE_ADMIN')),
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla: evaluaciones_riesgo
CREATE TABLE IF NOT EXISTS evaluaciones_riesgo (
    id BIGSERIAL PRIMARY KEY,
    score INTEGER NOT NULL CHECK (score >= 300 AND score <= 950),
    nivel_riesgo VARCHAR(20) NOT NULL CHECK (nivel_riesgo IN ('BAJO', 'MEDIO', 'ALTO')),
    detalle_riesgo TEXT,
    aprobado BOOLEAN NOT NULL,
    motivo TEXT,
    relacion_cuota_ingreso DECIMAL(5, 4),
    fecha_evaluacion TIMESTAMP NOT NULL
);

-- Tabla: solicitudes_credito
CREATE TABLE IF NOT EXISTS solicitudes_credito (
    id BIGSERIAL PRIMARY KEY,
    afiliado_id BIGINT NOT NULL,
    monto DECIMAL(15, 2) NOT NULL CHECK (monto > 0),
    plazo_meses INTEGER NOT NULL CHECK (plazo_meses > 0),
    tasa_propuesta DECIMAL(5, 2) NOT NULL CHECK (tasa_propuesta >= 0),
    fecha_solicitud TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    evaluacion_id BIGINT,
    CONSTRAINT fk_solicitudes_afiliado FOREIGN KEY (afiliado_id) REFERENCES afiliados(id) ON DELETE CASCADE,
    CONSTRAINT fk_solicitudes_evaluacion FOREIGN KEY (evaluacion_id) REFERENCES evaluaciones_riesgo(id) ON DELETE SET NULL
);

-- Índices para solicitudes_credito
CREATE INDEX IF NOT EXISTS idx_solicitudes_afiliado ON solicitudes_credito(afiliado_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes_credito(estado);
CREATE INDEX IF NOT EXISTS idx_solicitudes_fecha ON solicitudes_credito(fecha_solicitud DESC);

-- Comentarios de tablas
COMMENT ON TABLE afiliados IS 'Afiliados de la cooperativa';
COMMENT ON TABLE usuarios IS 'Usuarios del sistema con autenticación';
COMMENT ON TABLE usuarios_roles IS 'Roles asignados a los usuarios';
COMMENT ON TABLE evaluaciones_riesgo IS 'Evaluaciones de riesgo crediticio';
COMMENT ON TABLE solicitudes_credito IS 'Solicitudes de crédito de los afiliados';

-- ============================================
-- PARTE 2: DATOS INICIALES
-- ============================================

-- Insertar afiliados de prueba
INSERT INTO afiliados (documento, nombre, salario, fecha_afiliacion, estado) VALUES
('1017654321', 'Juan Carlos Pérez', 3500000.00, '2023-01-15', 'ACTIVO'),
('1023456789', 'María Fernanda López', 4200000.00, '2023-03-20', 'ACTIVO'),
('1034567890', 'Pedro Alberto Gómez', 2800000.00, '2024-11-01', 'ACTIVO'),
('1045678901', 'Ana María Rodríguez', 5000000.00, '2022-06-10', 'ACTIVO'),
('1056789012', 'Carlos Eduardo Martínez', 3000000.00, '2023-08-25', 'INACTIVO')
ON CONFLICT (documento) DO NOTHING;

-- ============================================
-- USUARIOS DEL SISTEMA
-- ============================================
-- Password para todos: "password123"
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================

-- Usuario Admin
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@coopcredit.com', NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios_roles (usuario_id, rol) 
SELECT id, 'ROLE_ADMIN' FROM usuarios WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- Usuario Analista
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('analista', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'analista@coopcredit.com', NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios_roles (usuario_id, rol) 
SELECT id, 'ROLE_ANALISTA' FROM usuarios WHERE username = 'analista'
ON CONFLICT DO NOTHING;

-- Usuario Afiliado - Juan Carlos Pérez
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('juanperez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'juan.perez@example.com', 
 (SELECT id FROM afiliados WHERE documento = '1017654321'))
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios_roles (usuario_id, rol) 
SELECT id, 'ROLE_AFILIADO' FROM usuarios WHERE username = 'juanperez'
ON CONFLICT DO NOTHING;

-- Usuario Afiliado - María Fernanda López
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('marialopez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maria.lopez@example.com',
 (SELECT id FROM afiliados WHERE documento = '1023456789'))
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios_roles (usuario_id, rol) 
SELECT id, 'ROLE_AFILIADO' FROM usuarios WHERE username = 'marialopez'
ON CONFLICT DO NOTHING;

-- ============================================
-- RESUMEN DE USUARIOS DE PRUEBA
-- ============================================
-- | Usuario     | Password     | Rol          |
-- |-------------|--------------|--------------|
-- | admin       | password123  | ROLE_ADMIN   |
-- | analista    | password123  | ROLE_ANALISTA|
-- | juanperez   | password123  | ROLE_AFILIADO|
-- | marialopez  | password123  | ROLE_AFILIADO|
-- ============================================

-- Verificar datos insertados
SELECT 'Afiliados insertados:' AS info, COUNT(*) AS total FROM afiliados;
SELECT 'Usuarios insertados:' AS info, COUNT(*) AS total FROM usuarios;
SELECT 'Roles asignados:' AS info, COUNT(*) AS total FROM usuarios_roles;

-- Mostrar usuarios con sus roles
SELECT u.username, u.email, string_agg(ur.rol, ', ') as roles
FROM usuarios u
LEFT JOIN usuarios_roles ur ON u.id = ur.usuario_id
GROUP BY u.id, u.username, u.email
ORDER BY u.username;
