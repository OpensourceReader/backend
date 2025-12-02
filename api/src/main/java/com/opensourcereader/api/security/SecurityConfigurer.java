package com.opensourcereader.api.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/** 전략 패턴을 응용한 보안 설정 */
public interface SecurityConfigurer {

  void configure(HttpSecurity http);
}
