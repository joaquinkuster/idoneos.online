package com.app.idoneos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=demo-id",
    "spring.security.oauth2.client.registration.google.client-secret=demo-secret"
})
class IdoneosApplicationTests {

	@Test
	void contextLoads() {
	}

}
