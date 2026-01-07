package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pull_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullCode extends BaseEntity {

  @Column
  private Integer tagNumber;   //TODO 나중에 Pull로 대체해야함

  @Column
  private String classPath;

  @Column
  private String className;

  private PullCode(Integer tagNumber, String classPath, String className) {
    super();
    this.tagNumber = tagNumber;
    this.classPath = classPath;
    this.className = className;
  }

  public static PullCode of(Integer tagNumber, String classPath) {
    return new PullCode(tagNumber, classPath, extractClassName(classPath));
  }

  private static String extractClassName(String classPath) {
    int slash = classPath.lastIndexOf("/") + 1;
    int dot = classPath.lastIndexOf(".");
    return classPath.substring(slash, dot);
  }
}
