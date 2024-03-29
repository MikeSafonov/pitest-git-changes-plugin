package com.github.mikesafonov.pitest.git.changes.report;

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

import java.util.Optional;
import java.util.Properties;

public abstract class PRMutationResultListenerFactory implements MutationResultListenerFactory {
    @Override
    public MutationResultListener getListener(Properties props, ListenerArguments args) {
        return new PRMutationResultListener(
                getWriter(props, args).orElseGet(NoopReportWriter::new),
                new PRReportBuilder()
        );
    }

    protected abstract Optional<ReportWriter> getWriter(Properties props, ListenerArguments args);
}
