# Integrating an API with the authentication service

A consuming API acts as a **Resource Server**: it receives the JWT in the `Authorization` header, validates its signature and expiry, and applies authorization to its routes. Do not store the signing private key or trust a token merely because its contents can be decoded.

## 1. Dependencies

For a Java/Spring Boot API, include Spring Security and the Resource Server starter (which provides JOSE/JWT support):

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

## 2. Configure the JWKS URL

Use the public URL for the environment, not the local example:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${AUTH_JWKS_URI}
```

`AUTH_JWKS_URI` should point to `https://<host>/auth-server/public-key/jwks`. This endpoint publishes RSA public keys and their `kid` values. Also validate the issuer `AuthenticationService` in the decoder configuration; no signing secret is shared with the consumer.

Example of explicit issuer validation in the decoder:

```java
@Bean
JwtDecoder jwtDecoder(@Value("${AUTH_JWKS_URI}") String jwksUri) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
    decoder.setJwtValidator(
        JwtValidators.createDefaultWithIssuer("AuthenticationService"));
    return decoder;
}
```

## 3. Protect routes

Configure Spring Security to validate JWTs and require authentication on private routes. Minimal example:

```java
@Bean
SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/health").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt())
        .build();
}
```

In Spring code, read claims from the `Jwt` principal, such as `sub`, `user_id`, `scope`, `application_public_id`, and `application_role`. Check that `scope` is appropriate (`login_token` for users; `application_token` for applications) and enforce your own access rules. Do not accept refresh or recovery tokens as access tokens.

## 4. Integration flow

1. Obtain the application's `applicationPublicId` from an administrator. For password-based accounts, the frontend calls `/users` to register and `/login` to authenticate; the login body includes `applicationPublicId`.
2. Send the access JWT to your API:

   ```http
   Authorization: Bearer <token>
   ```

3. Before it expires, the client sends the `refreshToken` as Bearer to `POST /auth-server/token` and replaces both tokens with the returned values.
4. For Google login, the client starts `/auth-server/oauth2/authorization/google` and handles the callback configured in the Auth Server.
5. If the application's backend needs to authenticate as an application, request tokens with `POST /auth-server/application` and the `publicId` header; renew with `POST /auth-server/token/application` and the application refresh token.

The base path is `/auth-server`. Documented development URLs are `http://localhost:2310/auth-server` and `http://localhost:2300/auth-server` (Docker). Browser clients are also subject to the Auth Server's CORS policy; backend-to-backend calls are not restricted by CORS.

The client should retrieve JWKS keys and honor `kid` to support rotation. Auth Server keys are kept in memory; after a restart, tokens signed by previous keys may no longer be verifiable.
