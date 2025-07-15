package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final String ISSUER = "AuthenticationService";
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("-03:00");
    private final KeyManager keyManager;

    public TokenService(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String generateRecorverToken(ResetPassword resetPassword){
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(resetPassword.getUserEmail())
                    .withAudience("frindlyPaw")
                    .withClaim("scope", "recoverCode")
                    .sign(algorithm);
        }catch (JWTCreationException e){
            throw new IllegalStateException("Error while creating RecoverToken", e);
        }
    }

    public String generateToken(User user) {
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withAudience("frindlyPaw")
                    .withClaim("user_id", user.getUserId() != null ? user.getUserId().toString() : null)
                    .withClaim("scope", "login_token")
                    .withExpiresAt(LocalDateTime.now().plusMinutes(15).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error while creating JWT token", e);
        }
    }

    public String generateRefreshToken(User user) {
        try {
            Algorithm algorithm = createAlgorithm();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("type", "refresh")
                    .withExpiresAt(LocalDateTime.now().plusDays(7).toInstant(ZONE_OFFSET))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error while creating refresh token", e);
        }
    }

    public String validateRefreshToken(String refreshToken) {
        try {
            Algorithm algorithm = createAlgorithm();
            var verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "refresh")
                    .build();

            var decodedJWT = verifier.verify(refreshToken);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException _) {
            return null;
        }
    }

    public String validateToken(String token, String userId, String email) {
        try {
            Algorithm algorithm = createAlgorithm();
            var verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("user_id", userId)
                    .withSubject(email)
                    .build();

            var decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException _) {
            return null;
        }
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