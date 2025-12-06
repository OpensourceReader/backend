package com.opensourcereader.api.security.configurer;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.opensourcereader.api.security.SecurityConfigurer;

/** CORS(Cross-Origin Resource Sharing, 교차 출처 리소스 공유)에 대한 설정 */
@Component
@Order
public class CorsConfigurer implements SecurityConfigurer {

  @Override
  public void configure(HttpSecurity http) {
    try {
      http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    } catch (Exception e) {
      throw new RuntimeException("CORS Error", e);
    }
  }

  protected CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // 모든 도메인 요청에 대해 허용
    configuration.setAllowedOriginPatterns(List.of("*"));
    // HTTP 헤더 허용값
    configuration.setAllowedHeaders(
        List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCEPT,
            HttpHeaders.COOKIE));

    // 자격 증명 허용 설정
    configuration.setAllowCredentials(true);

    // HTTP 메서드 허용 설정
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // "/api/**" 패턴으로 들어오는 모든 요청에 위 CORS 환경 설정 적용
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
