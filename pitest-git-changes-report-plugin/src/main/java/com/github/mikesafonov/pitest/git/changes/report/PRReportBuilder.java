package com.github.mikesafonov.pitest.git.changes.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PRReportBuilder {
    private int total = 0;
    private int killed = 0;
    private int survived = 0;

    private final Map<MutatedClass, List<PRMutant>> mutants = new HashMap<>();

    public PRReportBuilder add(MutatedClass mutatedClass, PRMutant mutant) {
        if(mutant.isSurvived()) {
            return survived(mutatedClass, mutant);
        } else {
            return killed(mutatedClass, mutant);
        }
    }

    public PRReportBuilder killed(MutatedClass mutatedClass, PRMutant mutant) {
        total++;
        killed++;
        storeMutant(mutatedClass, mutant);
        return this;
    }

    public PRReportBuilder survived(MutatedClass mutatedClass, PRMutant mutant) {
        total++;
        survived++;
        storeMutant(mutatedClass, mutant);
        return this;
    }

    private void storeMutant(MutatedClass mutatedClass, PRMutant mutant) {
        mutants.computeIfAbsent(mutatedClass, c -> new ArrayList<>()).add(mutant);
    }

    public PRReport build() {
        return new PRReport(
                total,
                killed,
                survived,
                mutants
        );
    }
}
