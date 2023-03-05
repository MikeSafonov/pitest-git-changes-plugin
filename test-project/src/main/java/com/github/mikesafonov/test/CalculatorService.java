package com.github.mikesafonov.test;

public class CalculatorService {

    public int calc(int a, int b) {
        int sum = a + b;
        if (a > b) {
            return sum * a;
        }
        if(a == b) {
            System.out.println("EQUALS");
        }
        return sum * b;
    }
}
