package com.github.mikesafonov.pitest.git.changes.report;

import lombok.Value;

@Value
public class PRMutant {
    private boolean survived;
    private int lineNumber;
    private String description;
    private String mutator;
}
