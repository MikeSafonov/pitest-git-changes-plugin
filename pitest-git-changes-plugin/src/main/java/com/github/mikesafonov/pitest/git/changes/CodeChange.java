package com.github.mikesafonov.pitest.git.changes;

import lombok.Value;

@Value
public class CodeChange {
    private final String className;
    private final int lineFrom;
    private final int lineTo;

    public boolean containsLine(int line) {
        return lineFrom >= line && lineTo <= line;
    }
}
