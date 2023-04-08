package com.github.mikesafonov.pitest.git.changes.report;

import org.junit.jupiter.api.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class PRMutationResultListenerTest {
    private final ReportWriter writer = mock(ReportWriter.class);
    private final PRReportBuilder reportBuilder = mock(PRReportBuilder.class);
    private final PRMutationResultListener listener = new PRMutationResultListener(writer, reportBuilder);

    @Test
    void shouldDoNothingWhenRunStart() {
        listener.runStart();
        verifyNoInteractions(writer);
        verifyNoInteractions(reportBuilder);
    }

    @Test
    void shouldBuildReportAndPassToWriterWhenRunEnd() {
        PRReport report = new PRReport(10, 10, 0, Collections.emptyMap());
        when(reportBuilder.build()).thenReturn(report);
        listener.runEnd();

        verify(writer).write(report);
    }

    @Test
    void shouldDoesNotCallBuilderWhenNoMutations() {
        ClassMutationResults results = new ClassMutationResults(
                new ArrayList<>()
        );

        listener.handleMutationResult(results);
        verifyNoInteractions(reportBuilder);
    }

    @Test
    void shouldCreateExpectedMutants() {
        MutatedClass expectedClass = new MutatedClass(
                ClassName.fromClass(PRMutationResultListener.class).asInternalName(),
                "java"
        );
        ClassMutationResults results = new ClassMutationResults(
                Arrays.asList(
                        new MutationResult(
                                new MutationDetails(
                                        new MutationIdentifier(
                                                Location.location(ClassName.fromClass(PRMutationResultListener.class), "test", "test"),
                                                0,
                                                "mut1"
                                        ), "PRMutationResultListener.java", "desc1", 1, 0
                                ),
                                new MutationStatusTestPair(0, DetectionStatus.KILLED, "")
                        ),
                        new MutationResult(
                                new MutationDetails(
                                        new MutationIdentifier(
                                                Location.location(ClassName.fromClass(PRMutationResultListener.class), "test", "test"),
                                                0,
                                                "mut2"
                                        ), "PRMutationResultListener.java", "desc2", 2, 0
                                ),
                                new MutationStatusTestPair(0, DetectionStatus.SURVIVED, "")
                        )
                )
        );

        listener.handleMutationResult(results);

        verify(reportBuilder).add(expectedClass, new PRMutant(
                false, 1, "desc1", "mut1"
        ));
        verify(reportBuilder).add(expectedClass, new PRMutant(
                true, 2, "desc2", "mut2"
        ));
    }

}
