package com.github.mikesafonov.pitest.git.changes.report;

import org.junit.jupiter.api.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

import static org.junit.jupiter.api.Assertions.*;

public class PRMutantTest {
    @Test
    void shouldCreateSurvived() {
        MutationResult result = new MutationResult(
                new MutationDetails(
                        new MutationIdentifier(
                                Location.location(ClassName.fromClass(PRMutationResultListener.class), "test", "test"),
                                0,
                                "mut2"
                        ), "PRMutationResultListener.java", "desc2", 2, 0
                ),
                new MutationStatusTestPair(0, DetectionStatus.SURVIVED, "")
        );
        PRMutant survived = PRMutant.survived(result);
        assertTrue(survived.isSurvived());
        assertEquals(2, survived.getLineNumber());
        assertEquals("desc2", survived.getDescription());
        assertEquals("mut2", survived.getMutator());

        survived = PRMutant.of(result);
        assertTrue(survived.isSurvived());
        assertEquals(2, survived.getLineNumber());
        assertEquals("desc2", survived.getDescription());
        assertEquals("mut2", survived.getMutator());
    }

    @Test
    void shouldCreateKilled() {
        MutationResult result = new MutationResult(
                new MutationDetails(
                        new MutationIdentifier(
                                Location.location(ClassName.fromClass(PRMutationResultListener.class), "test", "test"),
                                0,
                                "mut2"
                        ), "PRMutationResultListener.java", "desc2", 2, 0
                ),
                new MutationStatusTestPair(0, DetectionStatus.KILLED, "")
        );
        PRMutant survived = PRMutant.killed(result);
        assertFalse(survived.isSurvived());
        assertEquals(2, survived.getLineNumber());
        assertEquals("desc2", survived.getDescription());
        assertEquals("mut2", survived.getMutator());

        survived = PRMutant.of(result);
        assertFalse(survived.isSurvived());
        assertEquals(2, survived.getLineNumber());
        assertEquals("desc2", survived.getDescription());
        assertEquals("mut2", survived.getMutator());
    }
}
