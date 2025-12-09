package com.opensourcereader.core.analysis.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum OpenSourceRepoErrorCode {
  REPOSITORY_CLONE_FAILED(HttpStatus.BAD_GATEWAY, "원격 저장소 클론에 실패했습니다."),
  REPOSITORY_TREE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로컬 Git 저장소의 트리 구조 파싱에 실패했습니다."),
  REPOSITORY_OPEN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로컬 Git 저장소를 여는 데 실패했습니다."),
  REPOSITORY_TREE_ACCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로컬 Git 저장소의 Tree 객체를 불러올 없습니다."),
  REPOSITORY_REFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "로컬 Git 저장소내 해당 reference를 찾을 수 없습니다."),

  REPOSITORY_BLOB_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Git 파일 데이터를 불러오는 중 오류가 발생했습니다."),
  REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 저장소를 찾을 수 없습니다."),

  LOCAL_DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로컬 디렉터리 생성에 실패했습니다."),
  LOCAL_DIRECTORY_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로컬 디렉터리 삭제에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  OpenSourceRepoErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
