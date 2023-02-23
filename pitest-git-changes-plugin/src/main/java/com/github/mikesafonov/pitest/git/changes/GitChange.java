package com.github.mikesafonov.pitest.git.changes;

import lombok.Value;

@Value
public class GitChange {
    private final String fileName;
    private final int lineFrom;
    private final int lineTo;
}
