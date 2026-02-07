package com.opensourcereader.api;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

abstract class TestContainerSupport {

  private static final MySQLContainer<?> MYSQL_CONTAINER =
      new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass")
          .withEnv("MYSQL_ROOT_PASSWORD", "rootpass")
          .withCommand("--max-connections=200");

  static {
    MYSQL_CONTAINER.start();
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

    registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
  }
}
