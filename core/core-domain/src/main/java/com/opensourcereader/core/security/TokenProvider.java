package com.opensourcereader.core.security;

public interface TokenProvider {

  String encoder(String nickname, String email);

  String decode(String token);
}
