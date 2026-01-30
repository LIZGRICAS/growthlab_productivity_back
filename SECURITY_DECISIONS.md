# Explicación breve de decisiones de seguridad

## Observaciones (diagnóstico rápido)
- Dependencias: usa `spring-boot-starter-security` (Spring Boot 3.1.4) y `io.jsonwebtoken:jjwt` (0.11.5).
- JWT: la aplicación genera y valida tokens con `JwtService` usando HMAC (`Keys.hmacShaKeyFor(secret.getBytes())`). El `secret` por defecto en `application.properties` es `change_this_secret_for_prod` (valor inseguro si no se sobrescribe por variable de entorno).
- Expiración JWT: configurable via `security.jwt.expiration` (default 3600000 ms = 1h).
- Hash de contraseñas: `BCryptPasswordEncoder` presente y usado (buena práctica por defecto).
- CSRF: deshabilitado en `SecurityConfig` (común en APIs REST que usan tokens).
- Endpoints públicos: `/api/auth/**`, `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` están permitidos.
- `httpBasic` está habilitado (`httpBasic(Customizer.withDefaults())`).
- No se observa configuración explícita de CORS, políticas de cabeceras de seguridad, ni forzado de HTTPS en la app (posiblemente delegado a infraestructura).

## Decisiones de seguridad actuales (resumen)
- Uso de JWT HMAC (simetrico) para autenticación, con secreto configurable por env var.
- Contraseñas almacenadas como hash BCrypt.
- CSRF deshabilitado para permitir uso de tokens en clientes no basados en formularios.
- Endpoints de documentación y autenticación expuestos públicamente.
- Basic Auth activado (por `httpBasic`), además de JWT.

## Riesgos identificados
- Secreto JWT débil/default: `security.jwt.secret` por defecto es inseguro y puede ser demasiado corto para HMAC-SHA (Keys.hmacShaKeyFor requiere clave con entropía adecuada). Si el secreto es corto, firma de tokens es vulnerable.
- HMAC simétrico vs. claves asimétricas: con HMAC, cualquier servicio con el secreto puede firmar tokens; mayor riesgo en entornos distribuidos.
- `httpBasic` habilitado: abre vector adicional de autenticación que puede ser innecesario y confundir la política de autenticación.
- Ausencia de CORS/seguridad de cabeceras: si la app se expone a navegadores, es recomendable restringir orígenes y añadir cabeceras como `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`.
- Manejo de secretos en `application.properties`: aunque están parametrizados, el archivo contiene valores por defecto; es crítico asegurar variables de entorno/secret manager en producción.
- CSRF deshabilitado: apropiado para API si se usan headers Authorization, pero documentar claramente el modelo de cliente y validar que no se usan cookies de sesión.

## Recomendaciones (prioritizadas y accionables)
1. Clave JWT segura y gestión de secretos
   - Exigir un secreto con suficiente longitud/entropía (al menos 256 bits) y validar su longitud en arranque.
   - Mover secretos a un gestor de secretos (Vault, Azure Key Vault, AWS Secrets Manager) y evitar defaults en repositorio.
   - Considerar uso de JWT asimétrico (RS256/ES256) para separación de firma/verificación entre servicios.

2. Revisar/retirar `httpBasic`
   - Si no se usa Basic Auth, eliminar `httpBasic(Customizer.withDefaults())` para reducir superficie de ataque.

3. Fortalecer configuración de seguridad HTTP
   - Añadir cabeceras de seguridad (`X-Content-Type-Options`, `Strict-Transport-Security`, `X-Frame-Options`, `Referrer-Policy`, `Content-Security-Policy`).
   - Forzar HTTPS en la capa de aplicación o documentar requisito en la infraestructura (reverse proxy, load balancer).

4. CORS y CSRF
   - Definir políticas CORS explícitas que permitan solo orígenes confiables (no usar `*` en producción).
   - Mantener CSRF deshabilitado solo si el cliente siempre usa Authorization header y no depende de cookies de sesión.

5. Contraseñas y autenticación
   - Configurar la fuerza de BCrypt si es necesario (`new BCryptPasswordEncoder(strength)`), balanceando seguridad y rendimiento.
   - Implementar bloqueo por intentos fallidos y mecanismos de recuperación seguros (no revelar si usuario existe).

6. Validación y registro
   - Evitar loguear información sensible (tokens, contraseñas, passcodes) en todos los entornos.
   - Añadir monitoreo/rate-limiting en endpoints de autenticación para mitigar bruteforce.

7. Pruebas y dependencias
   - Mantener dependencias actualizadas y ejecutar escaneos SCA/OWASP Dependency-Check.
   - Añadir pruebas de seguridad automáticas (p. ej. tests para expiración/validación de tokens, intentos con token manipulado).

## Acciones propuestas a corto plazo (siguientes pasos)
- Forzar validación de longitud del secreto JWT en `JwtService` o durante arranque.
- Eliminar `httpBasic` si no es requerido y documentar la decisión.
- Configurar un `CorsConfigurationSource` restringido y añadir cabeceras de seguridad con `HttpSecurity`.
- Revisar `JwtAuthenticationFilter` (asegurarse de que descarta tokens malformados y no rellena el contexto con datos inseguros).

---
Documento generado automáticamente tras inspección de:
- [pom.xml](pom.xml)
- [src/main/resources/application.properties](src/main/resources/application.properties)
- [src/main/java/com/example/clevertap/security/JwtService.java](src/main/java/com/example/clevertap/security/JwtService.java)
- [src/main/java/com/example/clevertap/security/SecurityConfig.java](src/main/java/com/example/clevertap/security/SecurityConfig.java)
- [src/main/java/com/example/clevertap/controller/AuthController.java](src/main/java/com/example/clevertap/controller/AuthController.java)


# Implementaciones y ejemplos de seguridad


## 1) Validación del secreto JWT en arranque (implementado)

Fragmento de `JwtService` que valida que el secreto exista y tenga al menos 32 bytes (256 bits):

```java
@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be set and non-empty. Set 'security.jwt.secret' env var.");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret is too short: must be at least 256 bits (32 bytes) for HMAC-SHA algorithms.");
        }
    }
}
```

## 2) Quitar `httpBasic` y habilitar CORS restringido

Ejemplo de cambios en `SecurityConfig`:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService uds) throws Exception {
    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, uds);
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults()) // usa el bean corsConfigurationSource()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization","Content-Type"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

Ajusta `https://app.example.com` al dominio real del frontend en producción.

## 3) Añadir cabeceras de seguridad HTTP

Ejemplo de filtro para inyectar cabeceras de seguridad (regístralo en `SecurityConfig`):

```java
http.addFilterAfter(new OncePerRequestFilter() {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy", "default-src 'self'");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        filterChain.doFilter(request, response);
    }
}, JwtAuthenticationFilter.class);
```

## 4) Notas rápidas
- Mantén `CSRF` desactivado sólo si no se usan cookies de sesión.
- Evita loguear secretos o tokens en producción.
- Considera RS256/ES256 si quieres separar firma y verificación entre servicios.
