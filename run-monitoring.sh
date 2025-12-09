#!/bin/bash

# ==============================================================================
# COOPCREDIT - Script de Monitoreo
# ==============================================================================
# Este script inicia el stack de monitoreo completo:
# - PostgreSQL
# - Risk Central Mock Service
# - Credit Application Service
# - Prometheus
# - Grafana
# ==============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

check_docker() {
    print_info "Verificando Docker..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker no está corriendo. Por favor, inicia Docker Desktop."
        exit 1
    fi
    print_success "Docker está corriendo"
}

check_docker_compose() {
    print_info "Verificando Docker Compose..."
    if ! docker compose version > /dev/null 2>&1; then
        if ! docker-compose version > /dev/null 2>&1; then
            print_error "Docker Compose no está instalado."
            exit 1
        fi
    fi
    print_success "Docker Compose disponible"
}

start_monitoring() {
    print_header "INICIANDO STACK DE MONITOREO"
    
    cd "$PROJECT_DIR"
    
    print_info "Construyendo imágenes Docker..."
    docker compose -f docker-compose-monitoring.yml build
    
    print_info "Iniciando servicios..."
    docker compose -f docker-compose-monitoring.yml up -d
    
    print_success "Servicios iniciados"
}

wait_for_services() {
    print_header "ESPERANDO QUE LOS SERVICIOS ESTÉN LISTOS"
    
    print_info "Esperando a PostgreSQL..."
    until docker exec coopcredit-postgres pg_isready -U postgres > /dev/null 2>&1; do
        sleep 2
        echo -n "."
    done
    print_success "PostgreSQL listo"
    
    print_info "Esperando a Credit Application Service..."
    local max_attempts=60
    local attempt=0
    until curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; do
        sleep 3
        attempt=$((attempt + 1))
        echo -n "."
        if [ $attempt -ge $max_attempts ]; then
            print_warning "El servicio está tardando más de lo esperado..."
            break
        fi
    done
    print_success "Credit Application Service listo"
    
    print_info "Esperando a Prometheus..."
    until curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; do
        sleep 2
        echo -n "."
    done
    print_success "Prometheus listo"
    
    print_info "Esperando a Grafana..."
    until curl -s http://localhost:3000/api/health > /dev/null 2>&1; do
        sleep 2
        echo -n "."
    done
    print_success "Grafana listo"
}

show_urls() {
    print_header "🎉 STACK DE MONITOREO INICIADO"
    
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                    URLS DE ACCESO                            ║${NC}"
    echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}║${NC}                                                              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  🌐 ${GREEN}API Principal:${NC}     http://localhost:8080                ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  📚 ${GREEN}Swagger UI:${NC}        http://localhost:8080/swagger-ui.html${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  💚 ${GREEN}Health Check:${NC}      http://localhost:8080/actuator/health${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  📊 ${GREEN}Métricas:${NC}          http://localhost:8080/actuator/prometheus${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}                                                              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  🔥 ${YELLOW}Prometheus:${NC}        http://localhost:9090              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  📈 ${YELLOW}Grafana:${NC}           http://localhost:3000              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}     └── Usuario: ${GREEN}admin${NC} / Contraseña: ${GREEN}admin${NC}             ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}                                                              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  🎲 ${BLUE}Risk Central:${NC}      http://localhost:8081              ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}                                                              ${CYAN}║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Para detener los servicios:${NC}"
    echo -e "  docker compose -f docker-compose-monitoring.yml down"
    echo ""
    echo -e "${YELLOW}Para ver logs:${NC}"
    echo -e "  docker compose -f docker-compose-monitoring.yml logs -f"
    echo ""
}

stop_monitoring() {
    print_header "DETENIENDO STACK DE MONITOREO"
    
    cd "$PROJECT_DIR"
    docker compose -f docker-compose-monitoring.yml down
    
    print_success "Servicios detenidos"
}

show_status() {
    print_header "ESTADO DE LOS SERVICIOS"
    
    cd "$PROJECT_DIR"
    docker compose -f docker-compose-monitoring.yml ps
}

show_logs() {
    print_header "LOGS DE LOS SERVICIOS"
    
    cd "$PROJECT_DIR"
    docker compose -f docker-compose-monitoring.yml logs -f
}

main() {
    case "${1:-start}" in
        start)
            check_docker
            check_docker_compose
            start_monitoring
            wait_for_services
            show_urls
            ;;
        stop)
            stop_monitoring
            ;;
        restart)
            stop_monitoring
            start_monitoring
            wait_for_services
            show_urls
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        urls)
            show_urls
            ;;
        *)
            echo "Uso: $0 [comando]"
            echo ""
            echo "Comandos:"
            echo "  start   - Iniciar stack de monitoreo (default)"
            echo "  stop    - Detener stack de monitoreo"
            echo "  restart - Reiniciar stack de monitoreo"
            echo "  status  - Ver estado de los servicios"
            echo "  logs    - Ver logs en tiempo real"
            echo "  urls    - Mostrar URLs de acceso"
            exit 1
            ;;
    esac
}

main "$@"
