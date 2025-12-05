package com.opensourcereader.core.security.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

  @Value("${jwt.secretKey}")
  private String secretKey;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpireSeconds;

  private Algorithm algorithm;
  private JWTVerifier jwtVerifier;

  private static final String CLAIM_NAME = "nickname";

  @PostConstruct
  public void init() {
    algorithm = Algorithm.HMAC256(secretKey);
    jwtVerifier = JWT.require(algorithm).build();
  }

  public String encoder(String nickname, String email) {
    Instant accessExpireDate = Instant.now().plusSeconds(accessTokenExpireSeconds);

    return JWT.create()
        .withSubject(email)
        .withClaim(CLAIM_NAME, nickname)
        .withExpiresAt(accessExpireDate)
        .sign(algorithm);
  }

  public String decode(String token) {
    DecodedJWT decodedJWT = validateJwt(token);
    return decode(decodedJWT);
  }

  public String decode(DecodedJWT token) {
    return token.getClaim(CLAIM_NAME).asString();
  }

  public DecodedJWT validateJwt(String token) {
    try {
      return jwtVerifier.verify(token);
    } catch (TokenExpiredException e) {
      throw new JWTVerificationException("Token Expired");
    } catch (SignatureVerificationException e) {
      throw new JWTVerificationException("Invalid Signature");
    } catch (Exception e) {
      throw new JWTVerificationException("Unknown Error");
    }
  }
}
