package com.github.mikesafonov.pitest.git.changes.report;

import org.pitest.util.Log;

import java.util.logging.Logger;

public class NoopReportWriter implements ReportWriter {
    private static final Logger LOGGER = Log.getLogger();

    @Override
    public void write(PRReport report) {
        LOGGER.info("Noop PR report");
    }
}
