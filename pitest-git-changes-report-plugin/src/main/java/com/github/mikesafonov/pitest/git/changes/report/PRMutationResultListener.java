package com.github.mikesafonov.pitest.git.changes.report;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class PRMutationResultListener implements MutationResultListener {
    private final ReportWriter writer;
    private final PRReportBuilder reportBuilder = new PRReportBuilder();

    @Override
    public void runStart() {

    }

    @Override
    public void handleMutationResult(ClassMutationResults results) {
        List<MutationResult> mutations = new ArrayList<>(results.getMutations());
        if (mutations.isEmpty()) {
            return;
        }
        MutatedClass mutatedClass = toMutatedClass(results.getMutatedClass(), mutations.get(0));
        for (MutationResult mutation : mutations) {
            PRMutant mutant = PRMutant.of(mutation);
            reportBuilder.add(mutatedClass, mutant);
        }
    }

    @Override
    public void runEnd() {
        writer.write(reportBuilder.build());
    }

    private MutatedClass toMutatedClass(ClassName className, MutationResult mutation) {
        String extension = FilenameUtils.getExtension(mutation.getDetails().getFilename());
        return new MutatedClass(
                className.asInternalName(),
                extension
        );
    }
}
