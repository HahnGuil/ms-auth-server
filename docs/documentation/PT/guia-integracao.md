# Integrar uma API ao serviço de autenticação

Uma API consumidora atua como **Resource Server**: recebe o JWT no header `Authorization`, valida assinatura e validade e aplica autorização às rotas. Não armazene a chave privada nem confie apenas em decodificar o conteúdo do token.

## 1. Dependências

Em uma API Java com Spring Boot, inclua Spring Security e o Resource Server (o segundo traz suporte JOSE/JWT):

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

## 2. Configure a URL JWKS

Use o endereço público do ambiente, não o endereço local do exemplo:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${AUTH_JWKS_URI}
```

`AUTH_JWKS_URI` deve apontar para `https://<host>/auth-server/public-key/jwks`. O endpoint publica as chaves públicas RSA e seus `kid`. Valide também o emissor `AuthenticationService` na configuração do decoder; não é necessário compartilhar segredo de assinatura.

Exemplo de validação explícita do emissor no decoder:

```java
@Bean
JwtDecoder jwtDecoder(@Value("${AUTH_JWKS_URI}") String jwksUri) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
    decoder.setJwtValidator(
        JwtValidators.createDefaultWithIssuer("AuthenticationService"));
    return decoder;
}
```

## 3. Proteja as rotas

Configure Spring Security para validar JWT e exigir autenticação nas rotas privadas. Exemplo mínimo:

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

Em código Spring, obtenha os claims do principal `Jwt`, por exemplo `sub`, `user_id`, `scope`, `application_public_id` e `application_role`. A API deve conferir que o `scope` é apropriado (`login_token` para usuário; `application_token` para aplicação) e aplicar suas próprias regras de acesso. Não aceite refresh ou recovery token como token de acesso.

## 4. Fluxo de integração

1. Obtenha do administrador o `applicationPublicId` da aplicação. Para contas com senha, o frontend chama `/users` para cadastro e `/login` para autenticação; o body de login inclui `applicationPublicId`.
2. Envie o JWT de acesso recebido nas chamadas à sua API:

   ```http
   Authorization: Bearer <token>
   ```

3. Antes do vencimento, o cliente envia o `refreshToken` como Bearer para `POST /auth-server/token` e substitui ambos os tokens pelos novos.
4. Para login Google, o cliente inicia `/auth-server/oauth2/authorization/google` e processa o callback configurado pelo Auth Server.
5. Se o backend da aplicação precisar autenticar-se como aplicação, solicite tokens com `POST /auth-server/application` e header `publicId`; renove com `POST /auth-server/token/application` e o refresh de aplicação.

O caminho base é `/auth-server`. Os endereços de desenvolvimento documentados são `http://localhost:2310/auth-server` e `http://localhost:2300/auth-server` (Docker). Navegadores também dependem da política CORS do Auth Server; chamadas backend-a-backend não são limitadas por CORS.

O cliente deve buscar as chaves JWKS e respeitar `kid` para suportar rotação. As chaves são mantidas em memória no Auth Server; após reinicialização, tokens assinados pelas chaves anteriores podem deixar de ser verificáveis.
