package com.github.mikesafonov.pitest.git.changes.report;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
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
        if(!results.getMutations().isEmpty()) {
            MutatedClass mutatedClass = toMutatedClass(results);
            for (MutationResult mutation : results.getMutations()) {
                if (mutation.getStatus().isDetected()) {
                    reportBuilder.killed(mutatedClass, toMutant(mutation, false));
                } else {
                    reportBuilder.survived(mutatedClass, toMutant(mutation, true));
                }
            }
        }
    }

    @Override
    public void runEnd() {
        writer.write(reportBuilder.build());
    }

    private MutatedClass toMutatedClass(ClassMutationResults results) {
        ClassName className = results.getMutatedClass();
        MutationResult firstMutation = results.getMutations().stream().findFirst().get();
        String extension = FilenameUtils.getExtension(firstMutation.getDetails().getFilename());
        return new MutatedClass(
                className.asInternalName(),
                extension
        );
    }

    private PRMutant toMutant(MutationResult result, boolean survived) {
        return new PRMutant(
                survived,
                result.getDetails().getLineNumber(),
                result.getDetails().getDescription(),
                result.getDetails().getMutator()
        );
    }
}
