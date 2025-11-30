package com.opensourcereader.core.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.Getter;

@Entity
@Getter
public class User {

  @Id @GeneratedValue private Long id;

  private String nickname;

  private String email;

  protected User() {}

  public static User of() {
    User user = new User();
    return user;
  }
}
