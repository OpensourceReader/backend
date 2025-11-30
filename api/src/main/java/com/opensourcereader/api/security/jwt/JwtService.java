package com.opensourcereader.api.security.jwt;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private String secretKey;

  private Algorithm algorithm;
  private JWTVerifier jwtVerifier;

}
