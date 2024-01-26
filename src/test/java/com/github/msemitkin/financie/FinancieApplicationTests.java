package com.github.msemitkin.financie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FinancieApplication.class)
@Disabled
class FinancieApplicationTests {

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

}
