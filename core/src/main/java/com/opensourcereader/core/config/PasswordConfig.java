package com.opensourcereader.core.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
  @Bean
  protected PasswordEncoder passwordEncoder() {
    String encodingId = "argon2";
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    // saltLength(16), hashLength(32), parallelism(1), memory(16384), iterations(2)
    encoders.put(encodingId, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    return new DelegatingPasswordEncoder(encodingId, encoders);
  }
}
