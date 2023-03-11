package com.github.mikesafonov.pitest.git.changes.report;

import lombok.Value;

@Value
public class MutatedClass {
    private String relativePath;
    private String extension;

    public String toRealName() {
        return relativePath + "." + extension;
    }
}
