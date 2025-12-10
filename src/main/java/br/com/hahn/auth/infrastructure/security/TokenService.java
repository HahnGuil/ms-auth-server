package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
import br.com.hahn.auth.application.service.TokenLogService;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private static final String ISSUER = "AuthenticationService";
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("-03:00");
    private static final long TOKEN_EXPIRATION_TIME_MINUTES = 15;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME_MINUTES = 30;
    private final KeyManager keyManager;
    private final TokenLogService tokenLogService;

    public String generateRecoverToken(ResetPassword resetPassword, TokenLog tokenLog){
        log.info("TokenService: Generate RecoverToken for user: {}, at: {}", tokenLog.getUserId(), Instant.now());
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(resetPassword.getUserEmail())
                    .withClaim("scope", tokenLog.getScopeToken().getValue())
                    .withClaim("user_id", tokenLog.getUserId().toString())
                    .withClaim("token_log_id", tokenLog.getIdTokenLog().toString())
                    .withClaim("token_log_date_request", tokenLog.getCreateDate().toString())
                    .withExpiresAt(tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        }catch (JWTCreationException e){
            log.error("TokenService: Error to generate recover token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), Instant.now());
            throw new IllegalStateException(ErrorsResponses.GENERATE_RECOVER_TOKEN_ERROR.getMessage(), e);
        }
    }

    public String generateToken(User user, TokenLog tokenLog) {
        log.info("TokenService: Generate token for user: {}, at: {}", user.getUserId(), Instant.now());
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("user_id", user.getUserId().toString())
                    .withClaim("token_log_id", tokenLog.getIdTokenLog().toString())
                    .withClaim("token_log_date_request", tokenLog.getCreateDate().toString())
                    .withClaim("scope", tokenLog.getScopeToken().getValue())
                    .withClaim("type_user", user.getTypeUser().toString())
                    .withClaim("applications", Optional.ofNullable(user.getApplications())
                            .orElse(Set.of()).stream()
                            .map(Application::getNameApplication)
                            .toList())
                    .withExpiresAt(tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("TokenService: Error to generate token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), Instant.now());
            throw new IllegalStateException(ErrorsResponses.GENERATE_TOKEN_ERROR.getMessage(), e);
        }
    }


    public String generateRefreshToken(User user, TokenLog tokenLog) {
        log.info("TokenService: Generate refresh token for user: {}, at: {}", user.getUserId(), Instant.now());
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("user_id", user.getUserId().toString())
                    .withClaim("scope", ScopeToken.REFRESH_TOKEN.getValue())
                    .withClaim("token_log_id", tokenLog.getIdTokenLog().toString())
                    .withExpiresAt(tokenLog.getCreateDate().plusMinutes(REFRESH_TOKEN_EXPIRATION_TIME_MINUTES).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("TokenService: Error to generate refresh token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), Instant.now());
            throw new IllegalStateException(ErrorsResponses.GENERATE_REFRESH_TOKEN_ERROR.getMessage(), e);
        }
    }

    public String validateToken(String token) {
        log.info("TokenService: Starting validate token at: {}", Instant.now());
        try {
            DecodedJWT decodedJWT = decodeAndVerifyToken(token);
            validateTokenExistence(decodedJWT);
            return decodedJWT.getSubject();
        } catch (Exception _) {
            log.error("TokenService: Token invalid. Throw InvalidCredentialsException at: {}", Instant.now());
            throw new InvalidCredentialsException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }
    }
    
    private DecodedJWT decodeAndVerifyToken(String token) {
        log.info("TokenService: Starting Decode and Verify token at: {}", Instant.now());
        Algorithm algorithm = createAlgorithm();
        var verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }


    private void validateTokenExistence(DecodedJWT decodeToken){
        log.info("TokenService: Validate if token exists at: {}", Instant.now());
        var tokenLogId = decodeToken.getClaim("token_log_id").asString();

        log.info("TokenService: Verify if token is not null and if token exists at: {}", Instant.now());
        if (tokenLogId == null || !existsLoginLogFromToken(tokenLogId)) {
            log.error("TokenService: Token null or don't exists. Throw InvalidCredentialsException at: {}", Instant.now());
            throw new InvalidCredentialsException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }

        log.info("TokenService: Verify if token is REFRESH at: {}", Instant.now());
        if(!decodeToken.getClaim("scope").asString().equals(ScopeToken.REFRESH_TOKEN.getValue())
                && !validateTimeExpirationToken(tokenLogId)){
            log.error("TokenService: Token invalid, throw InvalidRefreshTokenException at: {}", Instant.now());
            throw new InvalidRefreshTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }
    }

    private boolean existsLoginLogFromToken(String tokenLogId){
        log.info("TokenService: Call TokenLogService to find token for id at: {}", Instant.now());
        return tokenLogService.existsById(UUID.fromString(tokenLogId));
    }

    private boolean validateTimeExpirationToken(String tokenLogId){
        log.info("TokenService: Call TokenLogService to find tokenLog expirationTime at: {}", Instant.now());
        var tokenLog = tokenLogService.findById(UUID.fromString(tokenLogId));
        var expirationTime = tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES);
        return expirationTime.isAfter(LocalDateTime.now());
    }

    private Algorithm createAlgorithm() {
        log.info("TokenService: Create Algorithm at: {}", Instant.now());
        return Algorithm.RSA256(new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                return (RSAPublicKey) keyManager.getPublicKeys().get(keyId);
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                var kp = keyManager.getCurrentKeyPair();
                if (kp == null || kp.getPrivate() == null) {
                    log.error("TokenService: Private key not available, throw IllegalStateException at: {}", Instant.now());
                    throw new IllegalStateException(ErrorsResponses.PRIVATE_KEY_NOT_AVAILABLE.getMessage());
                }
                return (RSAPrivateKey) kp.getPrivate();
            }

            @Override
            public String getPrivateKeyId() {
                var id = keyManager.getCurrentKeyId();
                if (id == null) {
                    log.error("TokenService: Private key ID not available, throw IllegalStateException at: {}", Instant.now());
                    throw new IllegalStateException(ErrorsResponses.ID_PRIVATE_KEY_NOT_AVAILABLE.getMessage());
                }
                return id;
            }
        });
    }
}