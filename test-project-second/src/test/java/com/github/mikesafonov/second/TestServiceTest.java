package com.github.mikesafonov.second;

import com.github.mikesafonov.second.TestService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestServiceTest {
    private TestService testService = new TestService();

    @Test
    void shouldReturn50() {
        Assertions.assertEquals(50, testService.calc(5, 5));
    }
}
