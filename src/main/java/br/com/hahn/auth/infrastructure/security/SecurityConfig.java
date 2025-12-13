package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.infrastructure.exception.CustomAccessDeniedHandler;
import br.com.hahn.auth.infrastructure.exception.CustomAuthenticationEntryPointHandler;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final SecurityFilter securityFilter;
    private final AuthService authService;

    public SecurityConfig(CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler, SecurityFilter securityFilter, @Lazy AuthService authService) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPointHandler = customAuthenticationEntryPointHandler;
        this.securityFilter = securityFilter;
        this.authService = authService;
    }

    /**
     * Configure the application's HTTP security and build the SecurityFilterChain.
     *
     * <p>This method:
     * <ul>
     *   <li>Disables CSRF protection as the application uses stateless authentication.</li>
     *   <li>Sets session management to STATELESS (no HTTP session is created).</li>
     *   <li>Defines public endpoints (health, info, login, user creation, password flows, JWKS and API docs)
     *       and requires authentication for all other requests.</li>
     *   <li>Registers custom handlers for authentication entry point and access denied events.</li>
     *   <li>Configures OAuth2 login with a custom user service and success handler.</li>
     *   <li>Configures the application as an OAuth2 resource server validating JWTs.</li>
     *   <li>Adds a custom security filter before the UsernamePasswordAuthenticationFilter.</li>
     * </ul>
     * </p>
     *
     * @param http the HttpSecurity to configure
     * @return the built SecurityFilterChain
     * @throws Exception if an error occurs while building the security chain
     * @author HahnGuil
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/password/reset-request").permitAll()
                        .requestMatchers(HttpMethod.POST, "/password/validate-code").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public-key/jwks").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPointHandler)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService())
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                )
                .oauth2ResourceServer(resource -> resource.jwt(jwt -> log.info("SecurityConfig: Resource JWT: {}", jwt)))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Provides an OAuth2UserService bean for loading user details based on the OAuth2UserRequest.
     *
     * <p>This method determines the type of OAuth2UserRequest and delegates the user loading
     * process to the appropriate service:
     * <ul>
     *   <li>If the request is an instance of OidcUserRequest, it uses the OidcUserService to load the user.</li>
     *   <li>Otherwise, it uses the DefaultOAuth2UserService to load the user.</li>
     * </ul>
     * </p>
     *
     * @author HahnGuil
     * @return an OAuth2UserService instance for handling OAuth2 user requests
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return userRequest -> {
            if (userRequest instanceof OidcUserRequest oidcUserRequest) {
                OidcUserService oidcUserService = new OidcUserService();
                return (OAuth2User) oidcUserService.loadUser(oidcUserRequest);
            } else {
                DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
                return defaultOAuth2UserService.loadUser(userRequest);
            }
        };
    }

    /**
     * Provides a PasswordEncoder bean for encoding passwords.
     *
     * <p>This method returns an instance of BCryptPasswordEncoder, which is a
     * password hashing function that applies the BCrypt algorithm. It is widely
     * used for securely storing passwords.</p>
     *
     * @author HahnGuil
     * @return a PasswordEncoder instance using the BCrypt algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an AuthenticationManager bean for managing authentication processes.
     *
     * <p>This method retrieves the AuthenticationManager from the provided
     * AuthenticationConfiguration, which is responsible for managing the
     * authentication flow in the application.</p>
     *
     * @author HahnGuil
     * @param authenticationConfiguration the configuration object used to obtain the AuthenticationManager
     * @return the AuthenticationManager instance
     * @throws Exception if an error occurs while retrieving the AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * Provides an AuthenticationSuccessHandler bean for handling successful OAuth2 authentication.
     *
     * <p>This method creates a custom AuthenticationSuccessHandler that processes the authenticated
     * OAuth2 user and generates a JSON response containing user details and tokens.</p>
     *
     * <p>Steps performed:
     * <ul>
     *   <li>Logs the OAuth2 authentication request details.</li>
     *   <li>Extracts the authenticated OAuth2 user from the authentication token.</li>
     *   <li>Processes the OAuth2 user using the AuthService to generate a LoginResponse.</li>
     *   <li>Writes the LoginResponse as a JSON object to the HTTP response.</li>
     * </ul>
     * </p>
     *
     * @author HahnGuil
     * @return an AuthenticationSuccessHandler instance for handling OAuth2 authentication success
     */
    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("SecurityConfig: Receiving oAuth2Auth in request: {} at: {}", request, DateTimeConverter.formatInstantNow());
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = token.getPrincipal();

            LoginResponse loginResponse = authService.processOAuthUser(oAuth2User);

            response.setContentType("application/json");
            response.getWriter().write("{\"name\": \"" + loginResponse.getUserName() +
                    "\", \"email\": \"" + loginResponse.getEmail() +
                    "\", \"token\": \"" + loginResponse.getToken() +
                    "\", \"refreshToken\": \"" + loginResponse.getRefreshToken() + "\"}");

        };
    }
}