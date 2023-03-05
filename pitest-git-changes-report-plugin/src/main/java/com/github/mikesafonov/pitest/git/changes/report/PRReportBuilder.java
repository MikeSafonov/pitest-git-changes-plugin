package com.github.mikesafonov.pitest.git.changes.report;

import org.pitest.classinfo.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PRReportBuilder {
    private int total = 0;
    private int killed = 0;
    private int survived = 0;

    private final Map<ClassName, List<PRMutant>> mutants = new HashMap<>();

    public PRReportBuilder killed() {
        total++;
        killed++;
        return this;
    }

    public PRReportBuilder survived(ClassName className, PRMutant mutant) {
        total++;
        survived++;
        mutants.computeIfAbsent(className, c -> new ArrayList<>());
        mutants.get(className).add(mutant);
        return this;
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
