-- V1__create_schema.sql
-- Migraciones iniciales para el esquema de base de datos

-- Tabla: afiliados
CREATE TABLE afiliados (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(15) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    salario DECIMAL(15, 2) NOT NULL CHECK (salario > 0),
    fecha_afiliacion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_documento CHECK (LENGTH(documento) >= 6)
);

-- Índices para afiliados
CREATE INDEX idx_afiliados_documento ON afiliados(documento);
CREATE INDEX idx_afiliados_estado ON afiliados(estado);

-- Tabla: usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    afiliado_id BIGINT,
    CONSTRAINT fk_usuarios_afiliado FOREIGN KEY (afiliado_id) REFERENCES afiliados(id) ON DELETE SET NULL
);

-- Índices para usuarios
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_email ON usuarios(email);

-- Tabla: usuarios_roles
CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ROLE_AFILIADO', 'ROLE_ANALISTA', 'ROLE_ADMIN')),
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla: evaluaciones_riesgo
CREATE TABLE evaluaciones_riesgo (
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
CREATE TABLE solicitudes_credito (
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
CREATE INDEX idx_solicitudes_afiliado ON solicitudes_credito(afiliado_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes_credito(estado);
CREATE INDEX idx_solicitudes_fecha ON solicitudes_credito(fecha_solicitud DESC);

-- Comentarios de tablas
COMMENT ON TABLE afiliados IS 'Afiliados de la cooperativa';
COMMENT ON TABLE usuarios IS 'Usuarios del sistema con autenticación';
COMMENT ON TABLE usuarios_roles IS 'Roles asignados a los usuarios';
COMMENT ON TABLE evaluaciones_riesgo IS 'Evaluaciones de riesgo crediticio';
COMMENT ON TABLE solicitudes_credito IS 'Solicitudes de crédito de los afiliados';
