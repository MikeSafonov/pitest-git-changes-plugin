package com.github.mikesafonov.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorServiceTest {
    private CalculatorService calculatorService = new CalculatorService();

    @Test
    void shouldReturn50() {
        Assertions.assertEquals(50, calculatorService.calc(5, 5));
    }
}
