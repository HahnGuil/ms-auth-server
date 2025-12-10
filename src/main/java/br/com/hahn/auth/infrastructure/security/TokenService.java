package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
import br.com.hahn.auth.application.service.TokenLogService;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TokenService {

    private static final String ISSUER = "AuthenticationService";
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("-03:00");
    private final KeyManager keyManager;
    private final TokenLogService tokenLogService;
    private static final long TOKEN_EXPIRATION_TIME_MINUTES = 15;

    public String generateRecorverToken(ResetPassword resetPassword, TokenLog tokenLog){
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(resetPassword.getUserEmail())
                    .withClaim("scope", tokenLog.getScopeToken().getValue())
                    .withClaim("user_id", tokenLog.getUserId().toString())
                    .withExpiresAt(tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        }catch (JWTCreationException e){
            throw new IllegalStateException("Error while creating RecoverToken", e);
        }
    }

    public String generateToken(User user, TokenLog tokenLog) {
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("user_id", user.getUserId() != null ? user.getUserId().toString() : null)
                    .withClaim("loginLog_id", tokenLog.getIdLoginLog().toString())
                    .withClaim("loginLog_date_request", tokenLog.getCreateDate().toString())
                    .withClaim("scope", tokenLog.getScopeToken().getValue())
                    .withClaim("type_user", user.getTypeUser().toString())
                    .withClaim("applications", Optional.ofNullable(user.getApplications())
                            .orElse(Set.of()).stream()
                            .map(Application::getNameApplication)
                            .toList())
                    .withExpiresAt(tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error while creating JWT token", e);
        }
    }


    public String generateRefreshToken(User user, TokenLog tokenLog) {
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("type", "refresh")
                    .withClaim("scope", ScopeToken.REFRESH_TOKEN.getValue())
                    .withClaim("loginLog_id", tokenLog.getIdLoginLog().toString())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error while creating refresh token", e);
        }
    }

    public String validateToken(String token) {
        try {
            DecodedJWT decodedJWT = decodeAndVerifyToken(token);
            validateTokenExistence(decodedJWT);
            return decodedJWT.getSubject();
        } catch (Exception _) {
            throw new InvalidCredentialsException("Invalid Token, please login");
        }
    }

    public UUID extracLoginLogId(String token){
        DecodedJWT decodedJWT = decodeAndVerifyToken(token);
        String loginLogId = decodedJWT.getClaim("loginLog_id").asString();

        log.info("TokenServie: id do login extraido antes do retorno {}", loginLogId );
        return UUID.fromString(loginLogId);
    }


    private DecodedJWT decodeAndVerifyToken(String token) {
        Algorithm algorithm = createAlgorithm();
        var verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }


    private void validateTokenExistence(DecodedJWT decodeToken){
        String loginLogId = decodeToken.getClaim("loginLog_id").asString();

        if (loginLogId == null || !existsLoginLogFromToken(loginLogId)) {
            throw new InvalidCredentialsException("Invalid token, please log in to continue.");
        }

        if(!decodeToken.getClaim("scope").asString().equals(ScopeToken.REFRESH_TOKEN.getValue())
                && !validateTimeExpirationToken(loginLogId)){
            throw new InvalidRefreshTokenException("Token expired, please log in again to continue");
        }
    }

    private boolean existsLoginLogFromToken(String loginLogId){
        return tokenLogService.existsById(UUID.fromString(loginLogId));
    }

    private boolean validateTimeExpirationToken(String loginLogId){
        TokenLog tokenLog = tokenLogService.findById(UUID.fromString(loginLogId));
        return !tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES).isBefore(LocalDateTime.now());
    }


    private Algorithm createAlgorithm() {
        return Algorithm.RSA256(new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                return (RSAPublicKey) keyManager.getPublicKeys().get(keyId);
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                return (RSAPrivateKey) keyManager.getCurrentKeyPair().get().getPrivate();
            }

            @Override
            public String getPrivateKeyId() {
                return keyManager.getCurrentKeyId().get();
            }
        });
    }
}