package com.opensourcereader.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class IntegrationTestSupport extends TestContainerSupport {

}
