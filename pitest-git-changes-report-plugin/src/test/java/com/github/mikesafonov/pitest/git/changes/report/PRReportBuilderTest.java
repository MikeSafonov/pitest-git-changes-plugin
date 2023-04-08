package com.github.mikesafonov.pitest.git.changes.report;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PRReportBuilderTest {
    private final PRReportBuilder builder = new PRReportBuilder();

    @Test
    void shouldIncrementKilled() {
        MutatedClass mutatedClass = new MutatedClass("test", "java");
        PRMutant mutant = new PRMutant(false, 1, "desc", "mut");
        builder.add(mutatedClass, mutant);

        PRReport report = builder.build();

        assertEquals(1, report.getTotal());
        assertEquals(1, report.getKilled());
        assertEquals(0, report.getSurvived());
        assertEquals(1, report.getMutants().size());
        assertEquals(1, report.getMutants().get(mutatedClass).size());
        assertEquals(mutant, report.getMutants().get(mutatedClass).get(0));
    }

    @Test
    void shouldIncrementSurvived() {
        MutatedClass mutatedClass = new MutatedClass("test", "java");
        PRMutant mutant = new PRMutant(true, 1, "desc", "mut");
        builder.add(mutatedClass, mutant);

        PRReport report = builder.build();

        assertEquals(1, report.getTotal());
        assertEquals(0, report.getKilled());
        assertEquals(1, report.getSurvived());
        assertEquals(1, report.getMutants().size());
        assertEquals(1, report.getMutants().get(mutatedClass).size());
        assertEquals(mutant, report.getMutants().get(mutatedClass).get(0));
    }

    @Test
    void shouldAddToExistingList() {
        MutatedClass mutatedClass = new MutatedClass("test", "java");
        PRMutant mutantOne = new PRMutant(true, 1, "desc", "mut");
        PRMutant mutantTwo = new PRMutant(false, 1, "desc", "mut");
        builder.add(mutatedClass, mutantOne);
        builder.add(mutatedClass, mutantTwo);

        PRReport report = builder.build();

        assertEquals(2, report.getTotal());
        assertEquals(1, report.getKilled());
        assertEquals(1, report.getSurvived());
        assertEquals(1, report.getMutants().size());
        assertEquals(2, report.getMutants().get(mutatedClass).size());
        assertEquals(mutantOne, report.getMutants().get(mutatedClass).get(0));
        assertEquals(mutantTwo, report.getMutants().get(mutatedClass).get(1));
    }
}
