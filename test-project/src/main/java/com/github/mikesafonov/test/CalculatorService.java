package com.github.mikesafonov.test;

public class CalculatorService {

    public int calc(int a, int b) {
        int sum = a + b;
        if (a > b) {
            a++;
            return sum * a;
        }
        return sum * b;
    }
}
