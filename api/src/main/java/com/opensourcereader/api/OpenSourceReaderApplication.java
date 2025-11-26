package com.opensourcereader.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackages = "com.opensourcereader.core")
@SpringBootApplication
public class OpenSourceReaderApplication {

  public static void main(String[] args) {
    SpringApplication.run(OpenSourceReaderApplication.class, args);
  }
}
