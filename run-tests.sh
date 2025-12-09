#!/bin/bash

# ==============================================================================
# COOPCREDIT - Script de Ejecución de Pruebas
# ==============================================================================
# Este script ejecuta todas las pruebas del proyecto
# Uso: ./run-tests.sh [opción]
# Opciones:
#   all       - Ejecutar todas las pruebas (unitarias + integración)
#   unit      - Solo pruebas unitarias
#   integration - Solo pruebas de integración
#   coverage  - Generar reporte de cobertura
# ==============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directorio del proyecto
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
CREDIT_SERVICE_DIR="$PROJECT_DIR/credit-application-service"
RISK_SERVICE_DIR="$PROJECT_DIR/risk-central-mock-service"

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

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Función para ejecutar pruebas unitarias
run_unit_tests() {
    print_header "EJECUTANDO PRUEBAS UNITARIAS"
    
    cd "$CREDIT_SERVICE_DIR"
    
    echo "📦 Ejecutando pruebas unitarias del Credit Application Service..."
    mvn test -Dtest="*Test" -DexcludedGroups="integration" \
        -Dspring.profiles.active=test \
        --batch-mode \
        -DskipITs=true
    
    if [ $? -eq 0 ]; then
        print_success "Pruebas unitarias completadas exitosamente"
    else
        print_error "Algunas pruebas unitarias fallaron"
        exit 1
    fi
}

# Función para ejecutar pruebas de integración
run_integration_tests() {
    print_header "EJECUTANDO PRUEBAS DE INTEGRACIÓN"
    
    cd "$CREDIT_SERVICE_DIR"
    
    echo "🐳 Verificando que Docker esté corriendo para Testcontainers..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker no está corriendo. Las pruebas de integración requieren Docker."
        exit 1
    fi
    
    echo "📦 Ejecutando pruebas de integración..."
    mvn test -Dtest="*IntegrationTest,*E2EIntegrationTest" \
        -Dspring.profiles.active=test \
        --batch-mode
    
    if [ $? -eq 0 ]; then
        print_success "Pruebas de integración completadas exitosamente"
    else
        print_error "Algunas pruebas de integración fallaron"
        exit 1
    fi
}

# Función para ejecutar todas las pruebas
run_all_tests() {
    print_header "EJECUTANDO TODAS LAS PRUEBAS"
    
    cd "$CREDIT_SERVICE_DIR"
    
    echo "🐳 Verificando Docker para Testcontainers..."
    if ! docker info > /dev/null 2>&1; then
        print_warning "Docker no está corriendo. Solo se ejecutarán pruebas unitarias."
        run_unit_tests
        return
    fi
    
    echo "📦 Ejecutando todas las pruebas..."
    mvn clean test \
        -Dspring.profiles.active=test \
        --batch-mode
    
    if [ $? -eq 0 ]; then
        print_success "Todas las pruebas completadas exitosamente"
    else
        print_error "Algunas pruebas fallaron"
        exit 1
    fi
}

# Función para generar reporte de cobertura
run_coverage() {
    print_header "GENERANDO REPORTE DE COBERTURA"
    
    cd "$CREDIT_SERVICE_DIR"
    
    echo "📊 Ejecutando pruebas con cobertura JaCoCo..."
    mvn clean test jacoco:report \
        -Dspring.profiles.active=test \
        --batch-mode
    
    if [ $? -eq 0 ]; then
        print_success "Reporte de cobertura generado"
        echo ""
        echo "📁 Ver reporte en: $CREDIT_SERVICE_DIR/target/site/jacoco/index.html"
    else
        print_error "Error generando reporte de cobertura"
        exit 1
    fi
}

# Función para ejecutar pruebas del Risk Service
run_risk_service_tests() {
    print_header "EJECUTANDO PRUEBAS DEL RISK CENTRAL MOCK SERVICE"
    
    cd "$RISK_SERVICE_DIR"
    
    echo "📦 Ejecutando pruebas..."
    mvn clean test --batch-mode
    
    if [ $? -eq 0 ]; then
        print_success "Pruebas del Risk Service completadas"
    else
        print_error "Algunas pruebas del Risk Service fallaron"
        exit 1
    fi
}

# Función principal
main() {
    print_header "COOPCREDIT - SUITE DE PRUEBAS"
    
    echo "📍 Directorio del proyecto: $PROJECT_DIR"
    echo "📍 Java version: $(java -version 2>&1 | head -n 1)"
    echo "📍 Maven version: $(mvn -version 2>&1 | head -n 1)"
    echo ""
    
    case "${1:-all}" in
        unit)
            run_unit_tests
            ;;
        integration)
            run_integration_tests
            ;;
        all)
            run_all_tests
            ;;
        coverage)
            run_coverage
            ;;
        risk)
            run_risk_service_tests
            ;;
        full)
            run_risk_service_tests
            run_all_tests
            run_coverage
            ;;
        *)
            echo "Uso: $0 [opción]"
            echo ""
            echo "Opciones:"
            echo "  all         - Ejecutar todas las pruebas (default)"
            echo "  unit        - Solo pruebas unitarias"
            echo "  integration - Solo pruebas de integración"
            echo "  coverage    - Generar reporte de cobertura"
            echo "  risk        - Pruebas del Risk Central Mock Service"
            echo "  full        - Todo: Risk + All + Coverage"
            exit 1
            ;;
    esac
    
    echo ""
    print_header "RESUMEN"
    print_success "Proceso completado exitosamente"
    echo ""
}

main "$@"
