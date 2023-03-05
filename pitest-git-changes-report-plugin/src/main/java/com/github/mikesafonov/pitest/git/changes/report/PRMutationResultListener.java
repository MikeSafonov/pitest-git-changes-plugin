package com.github.mikesafonov.pitest.git.changes.report;

import lombok.RequiredArgsConstructor;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;

@RequiredArgsConstructor
public class PRMutationResultListener implements MutationResultListener {
    private final ReportWriter writer;
    private final PRReportBuilder reportBuilder = new PRReportBuilder();

    @Override
    public void runStart() {

    }

    @Override
    public void handleMutationResult(ClassMutationResults results) {
        ClassName mutatedClass = results.getMutatedClass();
        for (MutationResult mutation : results.getMutations()) {
            if (mutation.getStatus().isDetected()) {
                reportBuilder.killed();
            } else {
                reportBuilder.survived(mutatedClass, toMutant(mutation));
            }
        }
    }

    @Override
    public void runEnd() {
        writer.write(reportBuilder.build());
    }

    private PRMutant toMutant(MutationResult result) {
        return new PRMutant(
                result.getDetails().getLineNumber(),
                result.getDetails().getDescription()
        );
    }
}
