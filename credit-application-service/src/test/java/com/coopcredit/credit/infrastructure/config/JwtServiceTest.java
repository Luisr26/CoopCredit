package com.coopcredit.credit.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para JwtService.
 * Verifica la generación y validación de tokens JWT.
 */
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // Secret key en Base64 (mínimo 256 bits para HS256)
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "TestSecretKeyForJWTTokenGenerationAndValidationTest1234567890".getBytes()
    );
    private static final long TEST_EXPIRATION = 86400000L; // 24 horas

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }

    @Nested
    @DisplayName("Tests para generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("Debe generar token válido para un username")
        void generateToken_DebeGenerarTokenValido() {
            // Given
            String username = "testuser";

            // When
            String token = jwtService.generateToken(username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes: header.payload.signature
        }

        @Test
        @DisplayName("Debe generar token con claims adicionales")
        void generateToken_DebeGenerarTokenConClaimsAdicionales() {
            // Given
            String username = "admin";
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", "ADMIN");
            extraClaims.put("email", "admin@test.com");

            // When
            String token = jwtService.generateToken(extraClaims, username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Debe generar tokens diferentes para el mismo usuario en distintos momentos")
        void generateToken_DebeGenerarTokensDiferentes() throws InterruptedException {
            // Given
            String username = "testuser";

            // When
            String token1 = jwtService.generateToken(username);
            Thread.sleep(1100); // JWT usa segundos, necesitamos más de 1 segundo para diferente iat
            String token2 = jwtService.generateToken(username);

            // Then - Los tokens deben ser diferentes porque el iat (issued at) cambia
            // Si el tiempo entre generaciones es menor a 1 segundo, serán iguales
            // Por eso esperamos más de 1 segundo
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Tests para extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Debe extraer username del token correctamente")
        void extractUsername_DebeExtraerUsernameCorrectamente() {
            // Given
            String username = "juanperez";
            String token = jwtService.generateToken(username);

            // When
            String extractedUsername = jwtService.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("Debe lanzar excepción con token inválido")
        void extractUsername_DebeLanzarExcepcionConTokenInvalido() {
            // Given
            String invalidToken = "invalid.token.here";

            // When/Then
            assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción con token malformado")
        void extractUsername_DebeLanzarExcepcionConTokenMalformado() {
            // Given
            String malformedToken = "notavalidjwttoken";

            // When/Then
            assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Tests para isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Debe retornar true para token válido y username correcto")
        void isTokenValid_DebeRetornarTrueParaTokenValido() {
            // Given
            String username = "validuser";
            String token = jwtService.generateToken(username);

            // When
            boolean isValid = jwtService.isTokenValid(token, username);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando username no coincide")
        void isTokenValid_DebeRetornarFalseCuandoUsernameNoConcide() {
            // Given
            String username = "user1";
            String token = jwtService.generateToken(username);

            // When
            boolean isValid = jwtService.isTokenValid(token, "differentuser");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Debe lanzar excepción o retornar false para token expirado")
        void isTokenValid_DebeRetornarFalseParaTokenExpirado() {
            // Given
            JwtService expiredJwtService = new JwtService();
            ReflectionTestUtils.setField(expiredJwtService, "secret", TEST_SECRET);
            ReflectionTestUtils.setField(expiredJwtService, "jwtExpiration", -1000L); // Expiración negativa

            String username = "testuser";
            String expiredToken = expiredJwtService.generateToken(username);

            // When/Then - El token expirado puede lanzar excepción o retornar false
            try {
                boolean isValid = jwtService.isTokenValid(expiredToken, username);
                assertThat(isValid).isFalse();
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Es válido que lance ExpiredJwtException
                assertThat(e).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Tests de seguridad")
    class SeguridadTests {

        @Test
        @DisplayName("Debe rechazar token firmado con secreto diferente")
        void debeRechazarTokenConSecretoDiferente() {
            // Given
            JwtService otherJwtService = new JwtService();
            String differentSecret = Base64.getEncoder().encodeToString(
                    "DifferentSecretKeyForTesting1234567890ABCDEFGHIJKLMNOP".getBytes()
            );
            ReflectionTestUtils.setField(otherJwtService, "secret", differentSecret);
            ReflectionTestUtils.setField(otherJwtService, "jwtExpiration", TEST_EXPIRATION);

            String token = otherJwtService.generateToken("testuser");

            // When/Then
            assertThatThrownBy(() -> jwtService.extractUsername(token))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Token no debe contener información sensible visible")
        void tokenNoDebeContenerInfoSensibleVisible() {
            // Given
            String username = "secretuser";
            Map<String, Object> claims = new HashMap<>();
            claims.put("password", "shouldnotbehere");

            // When
            String token = jwtService.generateToken(claims, username);

            // Then
            // El payload está en Base64, pero no debe ser legible directamente
            assertThat(token).doesNotContain("shouldnotbehere");
        }
    }

    @Nested
    @DisplayName("Tests de extractClaim()")
    class ExtractClaimTests {

        @Test
        @DisplayName("Debe extraer fecha de expiración del token")
        void extractClaim_DebeExtraerFechaExpiracion() {
            // Given
            String username = "testuser";
            String token = jwtService.generateToken(username);

            // When
            java.util.Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new java.util.Date());
        }

        @Test
        @DisplayName("Debe extraer fecha de emisión del token")
        void extractClaim_DebeExtraerFechaEmision() {
            // Given
            String username = "testuser";
            String token = jwtService.generateToken(username);

            // When
            java.util.Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());

            // Then
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt).isBeforeOrEqualTo(new java.util.Date());
        }
    }
}
