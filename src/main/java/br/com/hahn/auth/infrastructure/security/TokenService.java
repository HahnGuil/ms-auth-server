package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
import br.com.hahn.auth.application.service.TokenLogService;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.util.DateTimeConverter;
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

    /**
     * Generates a JWT used for password recovery.
     *
     * <p>This method builds a signed JWT containing:
     * <ul>
     *   <li>issuer</li>
     *   <li>subject set to the user's email</li>
     *   <li>claims: scope, user_id, token_log_id, token_log_date_request</li>
     *   <li>an expiration time based on the token log creation date</li>
     * </ul>
     * The token is signed using the RSA algorithm provided by the current key pair.</p>
     *
     * @author HahnGuil
     * @param resetPassword object containing the user's email for which the recovery token is generated
     * @param tokenLog object containing token log metadata (scope, user id, token id, creation date)
     * @return a signed JWT string representing the recovery token
     * @throws IllegalStateException if there is an error creating the JWT or if signing material is unavailable
     */
    public String generateRecoverToken(ResetPassword resetPassword, TokenLog tokenLog){
        log.info("TokenService: Generate RecoverToken for user: {}, at: {}", tokenLog.getUserId(), DateTimeConverter.formatInstantNow());
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
            log.error("TokenService: Error to generate recover token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), DateTimeConverter.formatInstantNow());
            throw new IllegalStateException(ErrorsResponses.GENERATE_RECOVER_TOKEN_ERROR.getMessage(), e);
        }
    }

    /**
     * Generates a JWT token for the specified user.
     *
     * <p>This method creates a signed JWT containing:
     * <ul>
     *   <li>issuer</li>
     *   <li>subject set to the user's email</li>
     *   <li>claims: user_id, token_log_id, token_log_date_request, scope, type_user, applications</li>
     *   <li>an expiration time based on the token log creation date</li>
     * </ul>
     * The token is signed using the RSA algorithm provided by the current key pair.</p>
     *
     * @author HahnGuil
     * @param user the user for whom the token is being generated
     * @param tokenLog the token log containing metadata such as scope, token ID, and creation date
     * @return a signed JWT string representing the token
     * @throws IllegalStateException if there is an error creating the JWT or if signing material is unavailable
     */
    public String generateToken(User user, TokenLog tokenLog) {
        log.info("TokenService: Generate token for user: {}, at: {}", user.getUserId(), DateTimeConverter.formatInstantNow());
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
            log.error("TokenService: Error to generate token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), DateTimeConverter.formatInstantNow());
            throw new IllegalStateException(ErrorsResponses.GENERATE_TOKEN_ERROR.getMessage(), e);
        }
    }

    /**
     * Generates a refresh token for the specified user.
     *
     * <p>This method creates a signed JWT containing:
     * <ul>
     *   <li>issuer</li>
     *   <li>subject set to the user's email</li>
     *   <li>claims: user_id, scope, token_log_id</li>
     *   <li>an expiration time based on the token log creation date</li>
     * </ul>
     * The token is signed using the RSA algorithm provided by the current key pair.</p>
     *
     * @author HahnGuil
     * @param user the user for whom the refresh token is being generated
     * @param tokenLog the token log containing metadata such as token ID and creation date
     * @return a signed JWT string representing the refresh token
     * @throws IllegalStateException if there is an error creating the JWT or if signing material is unavailable
     */
    public String generateRefreshToken(User user, TokenLog tokenLog) {
        log.info("TokenService: Generate refresh token for user: {}, at: {}", user.getUserId(), DateTimeConverter.formatInstantNow());
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
            log.error("TokenService: Error to generate refresh token for user: {}, throw IllegalStateException at: {}", tokenLog.getUserId(), DateTimeConverter.formatInstantNow());
            throw new IllegalStateException(ErrorsResponses.GENERATE_REFRESH_TOKEN_ERROR.getMessage(), e);
        }
    }

    /**
     * Validates the provided JWT token.
     *
     * <p>This method decodes and verifies the given token, ensuring its validity.
     * It also checks the token's existence and expiration time. If the token is valid,
     * the method returns the subject (e.g., user email) contained in the token.</p>
     *
     * @author HahnGuil
     * @param token the JWT token to be validated
     * @return the subject of the token if validation is successful
     * @throws InvalidCredentialsException if the token is invalid or verification fails
     */
    public String validateToken(String token) {
        log.info("TokenService: Starting validate token at: {}", DateTimeConverter.formatInstantNow());
        try {
            DecodedJWT decodedJWT = decodeAndVerifyToken(token);
            validateTokenExistence(decodedJWT);
            return decodedJWT.getSubject();
        } catch (Exception _) {
            log.error("TokenService: Token invalid. Throw InvalidCredentialsException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidCredentialsException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }
    }

    /**
     * Decodes and verifies the provided JWT token.
     *
     * <p>This method uses the RSA algorithm to create a verifier for the token.
     * It ensures that the token was issued by the expected issuer and verifies its signature.</p>
     *
     * @author HahnGuil
     * @param token the JWT token to be decoded and verified
     * @return the decoded JWT if the token is valid
     */
    private DecodedJWT decodeAndVerifyToken(String token) {
        log.info("TokenService: Starting Decode and Verify token at: {}", DateTimeConverter.formatInstantNow());
        Algorithm algorithm = createAlgorithm();
        var verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }


    /**
     * Validates the existence and validity of the provided decoded JWT token.
     *
     * <p>This method checks if the token exists in the system and verifies its scope and expiration time.
     * If the token is invalid, appropriate exceptions are thrown.</p>
     *
     * @author HahnGuil
     * @param decodeToken the decoded JWT token to be validated
     * @throws InvalidCredentialsException if the token does not exist or is null
     * @throws InvalidRefreshTokenException if the token is not a refresh token and has expired
     */
    private void validateTokenExistence(DecodedJWT decodeToken){
        log.info("TokenService: Validate if token exists at: {}", DateTimeConverter.formatInstantNow());
        var tokenLogId = decodeToken.getClaim("token_log_id").asString();

        log.info("TokenService: Verify if token is not null and if token exists at: {}", DateTimeConverter.formatInstantNow());
        if (tokenLogId == null || !existsLoginLogFromToken(tokenLogId)) {
            log.error("TokenService: Token null or don't exists. Throw InvalidCredentialsException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidCredentialsException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }

        log.info("TokenService: Verify if token is REFRESH at: {}", DateTimeConverter.formatInstantNow());
        if(!decodeToken.getClaim("scope").asString().equals(ScopeToken.REFRESH_TOKEN.getValue())
                && !validateTimeExpirationToken(tokenLogId)){
            log.error("TokenService: Token invalid, throw InvalidRefreshTokenException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidRefreshTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }
    }

    /**
     * Checks if a login log exists for the given token ID.
     *
     * <p>This method calls the `TokenLogService` to verify the existence of a login log
     * associated with the provided token ID.</p>
     * 
     * @author HahnGuil
     * @param tokenLogId the ID of the token log to check
     * @return true if a login log exists for the given token ID, false otherwise
     */
    private boolean existsLoginLogFromToken(String tokenLogId){
        log.info("TokenService: Call TokenLogService to find token for id at: {}", DateTimeConverter.formatInstantNow());
        return tokenLogService.existsById(UUID.fromString(tokenLogId));
    }

    /**
     * Validates whether the token associated with the given token log ID has expired.
     *
     * <p>This method retrieves the token log using the provided token log ID and calculates
     * the expiration time by adding the predefined token expiration time to the token's creation date.
     * It then checks if the calculated expiration time is after the current time.</p>
     *
     * @author HahnGuil
     * @param tokenLogId the ID of the token log to validate
     * @return true if the token has not expired, false otherwise
     */
    private boolean validateTimeExpirationToken(String tokenLogId){
        log.info("TokenService: Call TokenLogService to find tokenLog expirationTime at: {}", DateTimeConverter.formatInstantNow());
        var tokenLog = tokenLogService.findById(UUID.fromString(tokenLogId));
        var expirationTime = tokenLog.getCreateDate().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES);
        return expirationTime.isAfter(LocalDateTime.now());
    }

    /**
     * Creates an RSA256 algorithm for signing and verifying JWT tokens.
     *
     * <p>This method initializes an `Algorithm.RSA256` instance using a custom `RSAKeyProvider`.
     * The `RSAKeyProvider` implementation retrieves the public and private keys as well as the private key ID
     * from the `KeyManager`. If the private key or its ID is unavailable, an `IllegalStateException` is thrown.</p>
     *
     * @author HahnGuil
     * @return an `Algorithm` instance configured for RSA256 signing and verification
     * @throws IllegalStateException if the private key or private key ID is not available
     */
    private Algorithm createAlgorithm() {
        log.info("TokenService: Create Algorithm at: {}", DateTimeConverter.formatInstantNow());
        return Algorithm.RSA256(new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                return (RSAPublicKey) keyManager.getPublicKeys().get(keyId);
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                var kp = keyManager.getCurrentKeyPair();
                if (kp == null || kp.getPrivate() == null) {
                    log.error("TokenService: Private key not available, throw IllegalStateException at: {}", DateTimeConverter.formatInstantNow());
                    throw new IllegalStateException(ErrorsResponses.PRIVATE_KEY_NOT_AVAILABLE.getMessage());
                }
                return (RSAPrivateKey) kp.getPrivate();
            }

            @Override
            public String getPrivateKeyId() {
                var id = keyManager.getCurrentKeyId();
                if (id == null) {
                    log.error("TokenService: Private key ID not available, throw IllegalStateException at: {}", DateTimeConverter.formatInstantNow());
                    throw new IllegalStateException(ErrorsResponses.ID_PRIVATE_KEY_NOT_AVAILABLE.getMessage());
                }
                return id;
            }
        });
    }
}