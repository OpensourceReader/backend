package com.opensourcereader.api.security.jwt;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JwtService {

  private String secretKey;

  private Algorithm algorithm;
  private JWTVerifier jwtVerifier;
}
