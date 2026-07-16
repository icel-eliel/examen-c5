package com.examenc5.cti;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "cti.enabled=false")
class CtiBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
