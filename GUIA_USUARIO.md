# 🚀 Guía de Usuario - CoopCredit

## Sistema Integral de Solicitudes de Crédito para Cooperativas

---

## 📦 Requisitos Previos

- Docker Desktop instalado y ejecutándose
- Puerto 8080, 8081 y 5432 libres

---

## 🔧 Levantar el Proyecto

### Paso 1: Navegar al directorio del proyecto
```bash
cd /home/Coder/IdeaProjects/CoopCredit
```

### Paso 2: Levantar todos los servicios
```bash
docker compose up -d
```

### Paso 3: Verificar que todo está corriendo
```bash
docker compose ps
```

Deberías ver 3 contenedores en estado "running":
- `coopcredit-postgres` - Base de datos
- `risk-central-mock` - Servicio de evaluación de riesgo
- `credit-application` - API principal

### Paso 4: Verificar salud del sistema
```bash
curl http://localhost:8080/actuator/health
```

---

## 🌐 URLs de Acceso

| Servicio | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html |
| **API Principal** | http://localhost:8080 |
| **Health Check** | http://localhost:8080/actuator/health |
| **Risk Service** | http://localhost:8081 |

---

## 👥 Usuarios del Sistema

### Credenciales de Acceso

| Usuario | Contraseña | Rol | Email |
|---------|------------|-----|-------|
| `admin` | `password123` | ADMIN | admin@coopcredit.com |
| `analista` | `password123` | ANALISTA | analista@coopcredit.com |
| `juanperez` | `password123` | AFILIADO | juan.perez@example.com |
| `marialopez` | `password123` | AFILIADO | maria.lopez@example.com |

---

## 🔐 Roles y Permisos

### ROLE_ADMIN (Administrador)
**Descripción:** Control total del sistema

| Acción | Permitido |
|--------|:---------:|
| Crear afiliados | ✅ |
| Editar afiliados | ✅ |
| Ver todos los afiliados | ✅ |
| Crear solicitudes de crédito | ✅ |
| Evaluar solicitudes | ✅ |
| Ver todas las solicitudes | ✅ |

---

### ROLE_ANALISTA (Analista de Crédito)
**Descripción:** Evalúa y gestiona solicitudes de crédito

| Acción | Permitido |
|--------|:---------:|
| Crear afiliados | ❌ |
| Editar afiliados | ❌ |
| Ver todos los afiliados | ✅ |
| Crear solicitudes de crédito | ✅ |
| Evaluar solicitudes | ✅ |
| Ver todas las solicitudes | ✅ |

---

### ROLE_AFILIADO (Cliente)
**Descripción:** Usuario final que solicita créditos

| Acción | Permitido |
|--------|:---------:|
| Crear afiliados | ❌ |
| Editar afiliados | ❌ |
| Ver su propio perfil | ✅ |
| Crear solicitudes de crédito | ✅ |
| Evaluar solicitudes | ❌ |
| Ver sus propias solicitudes | ✅ |

---

## 📝 Cómo Usar Swagger UI

### 1. Acceder a Swagger
Abre en tu navegador: http://localhost:8080/swagger-ui/index.html

### 2. Hacer Login
1. Busca la sección **"Autenticación"**
2. Expande `POST /auth/login`
3. Click **"Try it out"**
4. Ingresa las credenciales:
```json
{
  "username": "admin",
  "password": "password123"
}
```
5. Click **"Execute"**
6. **Copia el token** de la respuesta

### 3. Autorizar
1. Click en el botón **"Authorize"** 🔒 (arriba a la derecha)
2. Pega el token (solo el token, sin "Bearer")
3. Click **"Authorize"** → **"Close"**

### 4. Usar los Endpoints
Ya puedes probar todos los endpoints según tu rol.

---

## 🧪 Pruebas Rápidas con cURL

### Login (obtener token)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

### Listar afiliados
```bash
TOKEN="<tu-token-aquí>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/afiliados
```

### Crear solicitud de crédito
```bash
curl -X POST http://localhost:8080/api/solicitudes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "afiliadoId": 1,
    "monto": 10000000,
    "plazoMeses": 24,
    "tasaPropuesta": 12.5
  }'
```

### Evaluar solicitud
```bash
curl -X POST http://localhost:8080/api/solicitudes/1/evaluar \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Flujo de Evaluación de Crédito

```
1. Afiliado crea solicitud → Estado: PENDIENTE
                ↓
2. Analista/Admin evalúa
                ↓
3. Sistema consulta Risk Central (score de riesgo)
                ↓
4. Sistema aplica políticas internas:
   • Relación cuota/ingreso ≤ 40%
   • Monto ≤ 5x salario
   • Antigüedad ≥ 6 meses
   • Score de riesgo no debe ser ALTO
                ↓
5. Resultado → Estado: APROBADO o RECHAZADO
```

---

## 🛑 Comandos de Gestión

### Detener servicios
```bash
docker compose down
```

### Ver logs
```bash
docker compose logs -f
```

### Ver logs de un servicio específico
```bash
docker compose logs -f credit-application
```

### Reiniciar todo
```bash
docker compose down && docker compose up -d
```

### Reconstruir (después de cambios en código)
```bash
docker compose down && docker compose up --build -d
```

---

## 🗄️ Base de Datos

### Acceder a PostgreSQL
```bash
docker exec -it coopcredit-postgres psql -U postgres -d coopcredit
```

### Consultas útiles
```sql
-- Ver afiliados
SELECT * FROM afiliados;

-- Ver usuarios
SELECT u.username, u.email, ur.rol 
FROM usuarios u 
JOIN usuarios_roles ur ON u.id = ur.usuario_id;

-- Ver solicitudes con evaluación
SELECT s.id, a.nombre, s.monto, s.estado, e.score, e.nivel_riesgo
FROM solicitudes_credito s
JOIN afiliados a ON s.afiliado_id = a.id
LEFT JOIN evaluaciones_riesgo e ON s.evaluacion_id = e.id;
```

---

## ⚙️ Configuración

Las políticas de crédito se pueden modificar en:
`credit-application-service/src/main/resources/application.yml`

```yaml
coopcredit:
  politicas:
    relacion-cuota-ingreso-maxima: 0.40  # 40%
    multiplicador-salario-monto-maximo: 5
    antiguedad-minima-meses: 6
```

---

---

## 📊 MONITOREO CON PROMETHEUS Y GRAFANA

### Iniciar Stack de Monitoreo Completo

El proyecto incluye un docker-compose especial con Prometheus y Grafana para monitoreo completo.

#### Paso 1: Iniciar el Stack de Monitoreo
```bash
# Usando el script (recomendado)
chmod +x run-monitoring.sh
./run-monitoring.sh start

# O manualmente
docker compose -f docker-compose-monitoring.yml up -d
```

#### Paso 2: Esperar que los servicios estén listos
El script automáticamente espera, pero si lo haces manual:
```bash
# Verificar estado
docker compose -f docker-compose-monitoring.yml ps

# Verificar health del servicio principal
curl http://localhost:8080/actuator/health
```

### 🌐 URLs de Monitoreo

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Métricas** | http://localhost:8080/actuator/prometheus | - |
| **Health** | http://localhost:8080/actuator/health | - |

### 📈 Usar Grafana

1. **Acceder a Grafana:** http://localhost:3000
2. **Login:** `admin` / `admin`
3. **Dashboard:** Ya viene preconfigurado con el dashboard "CoopCredit - Application Metrics"

#### Métricas Disponibles en el Dashboard:
- 📊 **Request Rate**: Peticiones por segundo
- ⏱️ **Response Time**: Tiempo de respuesta
- 📈 **Total Requests**: Gráfico temporal
- 🔌 **Circuit Breaker Status**: Estado del circuit breaker
- 💾 **Heap Memory Usage**: Uso de memoria

### 🔥 Usar Prometheus

1. **Acceder a Prometheus:** http://localhost:9090
2. **Ver Targets:** http://localhost:9090/targets
3. **Consultas PromQL de ejemplo:**

```promql
# Tasa de peticiones HTTP
rate(http_server_requests_seconds_count[5m])

# Tiempo promedio de respuesta
http_server_requests_seconds_mean

# Solicitudes de crédito aprobadas
credit_solicitudes_aprobadas_total

# Estado del Circuit Breaker
resilience4j_circuitbreaker_state{name="risk-central"}

# Memoria usada
jvm_memory_used_bytes{area="heap"}
```

### 🔧 Probar el Circuit Breaker

Para verificar que el Circuit Breaker funciona correctamente:

```bash
# 1. Detener Risk Central Mock
docker compose -f docker-compose-monitoring.yml stop risk-central-mock-service

# 2. Evaluar una solicitud (usará el fallback)
TOKEN="<tu-token>"
curl -X POST http://localhost:8080/api/solicitudes/1/evaluar \
  -H "Authorization: Bearer $TOKEN"

# 3. Ver el estado del Circuit Breaker
curl http://localhost:8080/actuator/health | jq '.components.circuitBreakers'

# 4. Ver métricas del Circuit Breaker
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state

# 5. Reiniciar Risk Central
docker compose -f docker-compose-monitoring.yml start risk-central-mock-service
```

### 📊 Métricas Personalizadas de Negocio

El sistema expone métricas específicas del negocio:

| Métrica | Descripción |
|---------|-------------|
| `credit.solicitudes.creadas` | Total solicitudes creadas |
| `credit.solicitudes.aprobadas` | Total solicitudes aprobadas |
| `credit.solicitudes.rechazadas` | Total solicitudes rechazadas |
| `credit.evaluacion.tiempo` | Tiempo de evaluación |
| `credit.risk.central.calls` | Llamadas a Risk Central |
| `credit.risk.central.failures` | Fallos en Risk Central |

### Comandos del Script de Monitoreo

```bash
./run-monitoring.sh start   # Iniciar todo
./run-monitoring.sh stop    # Detener todo
./run-monitoring.sh restart # Reiniciar todo
./run-monitoring.sh status  # Ver estado
./run-monitoring.sh logs    # Ver logs en tiempo real
./run-monitoring.sh urls    # Mostrar URLs
```

---

## 🧪 EJECUTAR PRUEBAS

### Usando el Script de Pruebas (Recomendado)

```bash
# Dar permisos de ejecución
chmod +x run-tests.sh

# Ejecutar todas las pruebas
./run-tests.sh all

# Solo pruebas unitarias
./run-tests.sh unit

# Solo pruebas de integración (requiere Docker)
./run-tests.sh integration

# Generar reporte de cobertura
./run-tests.sh coverage

# Ejecutar todo: Risk Service + All Tests + Coverage
./run-tests.sh full
```

### Ejecutar Pruebas Manualmente

```bash
cd credit-application-service

# Todas las pruebas
mvn clean test

# Solo pruebas unitarias
mvn test -Dtest="*Test" -DskipITs=true

# Solo pruebas de integración
mvn test -Dtest="*IntegrationTest"

# Con reporte de cobertura
mvn clean test jacoco:report
# Ver reporte en: target/site/jacoco/index.html
```

### Tipos de Pruebas Incluidas

| Tipo | Archivos | Descripción |
|------|----------|-------------|
| **Unitarias** | `*Test.java` | Prueban clases individuales con mocks |
| **Integración** | `*IntegrationTest.java` | Prueban con MockMvc y Spring Context |
| **E2E** | `*E2EIntegrationTest.java` | Flujo completo con Testcontainers |

### Pruebas Disponibles

```
📁 Tests Unitarios:
├── AfiliadoServiceTest
├── SolicitudCreditoServiceTest
├── EvaluarSolicitudServiceTest
├── PoliticasCreditoServiceTest
└── JwtServiceTest

📁 Tests de Integración:
├── AfiliadoControllerIntegrationTest
├── SolicitudCreditoControllerIntegrationTest
├── AuthControllerIntegrationTest
└── CreditoE2EIntegrationTest (con Testcontainers)
```

### Ver Resultados de Pruebas

```bash
# Después de ejecutar mvn test
# Ver reporte HTML:
open target/surefire-reports/index.html

# Ver reporte de cobertura (después de jacoco:report):
open target/site/jacoco/index.html
```

---

## 📊 Monitoreo y Observabilidad

### Levantar Stack Completo con Monitoreo

Para ejecutar el sistema con Prometheus y Grafana:

```bash
# Opción 1: Usando el script (recomendado)
./run-monitoring.sh

# Opción 2: Manualmente
docker compose -f docker-compose-monitoring.yml up -d
```

### URLs del Stack de Monitoreo

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **API Principal** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **Métricas Prometheus** | http://localhost:8080/actuator/prometheus | - |
| **Prometheus** | http://localhost:9091 | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Risk Central** | http://localhost:8081 | - |

### Acceder al Dashboard de Grafana

1. Abrir http://localhost:3000
2. Login: `admin` / `admin`
3. Ir a **Dashboards** → **CoopCredit - Application Metrics**

### Paneles del Dashboard

| Panel | Descripción |
|-------|-------------|
| 🚀 **Throughput** | Requests por segundo |
| ⏱️ **Latencia P95** | Tiempo de respuesta percentil 95 |
| ✅ **Success Rate** | Porcentaje de requests exitosos |
| 🔌 **Circuit Breaker** | Estado (CLOSED/OPEN/HALF-OPEN) |
| 💚 **Service Health** | Estado UP/DOWN |
| 💾 **Heap Memory** | Uso de memoria JVM |
| 🧵 **Threads** | Hilos activos |
| 📊 **HTTP Status Distribution** | Distribución de códigos HTTP |

### Verificar Métricas

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Métricas en formato Prometheus
curl http://localhost:8080/actuator/prometheus | head -50

# Verificar targets en Prometheus
curl http://localhost:9091/api/v1/targets
```

### Generar Tráfico para Métricas

```bash
# Login como admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Hacer requests para generar métricas
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/afiliados
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/solicitudes
```

### Evidencia de Logs

Los logs del sistema muestran información estructurada:

```
2025-12-09 17:15:23 INFO  c.c.c.a.s.EvaluarSolicitudService - Evaluando solicitud ID: 1
2025-12-09 17:15:23 INFO  c.c.c.i.a.o.e.r.RiskCentralClient - Consultando risk-central para documento: 1017654311
2025-12-09 17:15:23 INFO  c.c.c.a.s.EvaluarSolicitudService - Evaluación completada: APROBADO
```

Ver logs en tiempo real:

```bash
# Logs del servicio principal
docker compose -f docker-compose-monitoring.yml logs -f credit-application

# Logs de todos los servicios
docker compose -f docker-compose-monitoring.yml logs -f
```

### Detener Stack de Monitoreo

```bash
docker compose -f docker-compose-monitoring.yml down

# Para eliminar también los volúmenes (datos)
docker compose -f docker-compose-monitoring.yml down -v
```

---

## 🆘 Solución de Problemas

### Puerto 5432 ocupado
```bash
# Detener PostgreSQL local
sudo systemctl stop postgresql
```

### Error de conexión
```bash
# Verificar contenedores
docker compose ps

# Ver logs del servicio
docker compose logs credit-application
```

### Limpiar todo y empezar de nuevo
```bash
docker compose down -v
docker compose up --build -d
```

---

**© 2025 CoopCredit - Sistema de Solicitudes de Crédito**
