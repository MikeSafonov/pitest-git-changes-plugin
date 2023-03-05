package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.changes.report.PRReport;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;

public class GithubReportWriter implements ReportWriter {
    @Override
    public void write(PRReport report) {
        System.out.println(report);
    }
}
