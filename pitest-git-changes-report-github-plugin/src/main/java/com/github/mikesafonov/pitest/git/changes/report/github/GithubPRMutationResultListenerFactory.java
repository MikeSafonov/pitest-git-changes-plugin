package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.changes.report.PRMutationResultListenerFactory;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;
import org.pitest.mutationtest.ListenerArguments;

import java.util.Properties;

public class GithubPRMutationResultListenerFactory extends PRMutationResultListenerFactory {
    @Override
    protected ReportWriter createWriter(Properties props, ListenerArguments args) {
        return new GithubReportWriter();
    }

    @Override
    public String name() {
        return "GITHUB";
    }

    @Override
    public String description() {
        return "Sending mutation report to Github Pull Request";
    }
}
