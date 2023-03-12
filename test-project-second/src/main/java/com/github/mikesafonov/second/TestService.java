package com.github.mikesafonov.second;

public class TestService {

    public int calc(int a, int b) {
        int sum = a + b;
        if (a > b) {
            return sum * a;
        }
        System.out.println(a);
        callMe(a, b);
        return sum * b;
    }

    private void callMe(int a, int b) {
        int i = a + b;
        System.out.println(i);
    }
}
