-- V2__insert_initial_data.sql
-- Datos iniciales del sistema

-- Insertar afiliados de prueba
INSERT INTO afiliados (documento, nombre, salario, fecha_afiliacion, estado) VALUES
('1017654321', 'Juan Carlos Pérez', 3500000.00, '2023-01-15', 'ACTIVO'),
('1023456789', 'María Fernanda López', 4200000.00, '2023-03-20', 'ACTIVO'),
('1034567890', 'Pedro Alberto Gómez', 2800000.00, '2024-11-01', 'ACTIVO'),
('1045678901', 'Ana María Rodríguez', 5000000.00, '2022-06-10', 'ACTIVO'),
('1056789012', 'Carlos Eduardo Martínez', 3000000.00, '2023-08-25', 'INACTIVO');

-- Insertar usuarios del sistema
-- Password: "password123" encriptado con BCrypt
-- Para generar: usar BCryptPasswordEncoder en Spring o herramientas online
-- Nota: Estos son hashes de ejemplo, en producción deben regenerarse

-- Admin user
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@coopcredit.com', NULL);

INSERT INTO usuarios_roles (usuario_id, rol) VALUES
((SELECT id FROM usuarios WHERE username = 'admin'), 'ROLE_ADMIN');

-- Analista user
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('analista', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'analista@coopcredit.com', NULL);

INSERT INTO usuarios_roles (usuario_id, rol) VALUES
((SELECT id FROM usuarios WHERE username = 'analista'), 'ROLE_ANALISTA');

-- Afiliado user (vinculado a Juan Carlos Pérez)
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('juanperez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'juan.perez@example.com', 
 (SELECT id FROM afiliados WHERE documento = '1017654321'));

INSERT INTO usuarios_roles (usuario_id, rol) VALUES
((SELECT id FROM usuarios WHERE username = 'juanperez'), 'ROLE_AFILIADO');

-- Afiliado user (vinculado a María Fernanda López)
INSERT INTO usuarios (username, password, email, afiliado_id) VALUES
('marialopez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maria.lopez@example.com',
 (SELECT id FROM afiliados WHERE documento = '1023456789'));

INSERT INTO usuarios_roles (usuario_id, rol) VALUES
((SELECT id FROM usuarios WHERE username = 'marialopez'), 'ROLE_AFILIADO');

-- Comentarios
COMMENT ON TABLE afiliados IS 'Usuarios de prueba creados: admin/password123, analista/password123, juanperez/password123, marialopez/password123';
