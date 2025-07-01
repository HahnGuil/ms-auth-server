package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.model.Role;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;

            String token = JWT.create()
                    .withIssuer("AuhenticationService")
                    .withSubject(user.getEmail())
                    .withClaim("role", roleName)
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);

            return token;
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error authenticating token");
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm).withIssuer("SMBuilding")
                    .build()
                    .verify(token)
                    .getSubject();

        }catch (JWTVerificationException e){
            return null;
        }
    }


}
