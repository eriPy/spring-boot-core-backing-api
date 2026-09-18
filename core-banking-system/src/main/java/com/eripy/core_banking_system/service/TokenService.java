package com.eripy.core_banking_system.service;

import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.eripy.core_banking_system.dto.IdRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

@Service 
public class TokenService {
    private final SecretKey secretKey;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value ("${jwt.expiration}")
    private long jwtExpiration;

    public TokenService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String createJwtToken(IdRequest idRequest, long time, SecretKey secretKey) {
        logger.info("Generated token");
        return Jwts.builder()
            .subject(String.valueOf(idRequest.id()))
            .claim("Account id", idRequest.id())
            .expiration(new Date(System.currentTimeMillis() + time))
            .signWith(secretKey)
            .compact();
    }

    public String validateJwtToken(String jwtToken) {
        try {
            logger.info("The token is being verified");
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
            return claims.getSubject();

        } catch (ExpiredJwtException e) {
            logger.error("token expired: " + e.getMessage());
            throw new JwtException("Token expired");
        } catch (MalformedJwtException e) {
            logger.error("Invalid token: " + e.getMessage());
            throw new JwtException("Invalid token");
        } catch (JwtException e) {
            logger.error("Failed to validate token: " + e.getMessage());
            throw new JwtException("Failed to validate token");
        }
    }
}