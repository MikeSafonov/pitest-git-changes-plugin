package com.github.mikesafonov.pitest.git.changes.report;

public interface ReportWriter extends AutoCloseable {
    void write(PRReport report);
}
