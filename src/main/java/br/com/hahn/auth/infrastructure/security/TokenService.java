package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.domain.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final String ISSUER = "AuthenticationService";
    private static final int EXPIRATION_MINUTES = 15;
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("-03:00");

    private final KeyManager keyManager;

    public TokenService(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String generateToken(User user) {
        try {
            String kid = keyManager.getCurrentKeyId();
            Algorithm algorithm = Algorithm.RSA256(new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String keyId) {
                    return (RSAPublicKey) keyManager.getPublicKeys().get(keyId);
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return (RSAPrivateKey) keyManager.getCurrentKeyPair().getPrivate();
                }

                @Override
                public String getPrivateKeyId() {
                    return kid;
                }
            });

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("user_id", user.getUserId() != null ? user.getUserId().toString() : null)
                    .withClaim("username", user.getUsername())
                    .withClaim("firstName", user.getFirstName())
                    .withClaim("lastName", user.getLastName())
                    .withExpiresAt(this.generateExpirationDate())
                    .withKeyId(kid)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error while creating JWT token", e);
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES).toInstant(ZONE_OFFSET);
    }

    public String validateToken(String token, String expectedUserId, String expectedEmail) {
        try {
            Algorithm algorithm = Algorithm.RSA256(new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String keyId) {
                    return (RSAPublicKey) keyManager.getPublicKeys().get(keyId);
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return null; // Not needed for verification
                }

                @Override
                public String getPrivateKeyId() {
                    return null;
                }
            });

            var verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withSubject(expectedEmail)
                    .build();

            var decodedJWT = verifier.verify(token);

            Instant expiresAt = decodedJWT.getExpiresAt().toInstant();
            if (expiresAt.isBefore(Instant.now())) {
                return null;
            }

            String tokenUserId = decodedJWT.getClaim("user_id").asString();
            String tokenEmail = decodedJWT.getSubject();
            if (!expectedUserId.equals(tokenUserId) || !expectedEmail.equals(tokenEmail)) {
                return null;
            }

            return tokenEmail;
        } catch (JWTVerificationException _) {
            // Log the exception if a logger is available
            return null;
        }
    }

    public String renewToken(User user) {
        return generateToken(user);
    }

}
