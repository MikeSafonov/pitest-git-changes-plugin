package com.github.mikesafonov.pitest.git.changes.report;

import lombok.Value;
import org.pitest.mutationtest.MutationResult;

@Value
public class PRMutant {
    private boolean survived;
    private int lineNumber;
    private String description;
    private String mutator;

    public static PRMutant of(MutationResult mutation) {
        boolean survived = !mutation.getStatus().isDetected();
        return new PRMutant(
                survived,
                mutation.getDetails().getLineNumber(),
                mutation.getDetails().getDescription(),
                mutation.getDetails().getMutator()
        );
    }
    public static PRMutant survived(MutationResult mutation) {
        return new PRMutant(
                true,
                mutation.getDetails().getLineNumber(),
                mutation.getDetails().getDescription(),
                mutation.getDetails().getMutator()
        );
    }

    public static PRMutant killed(MutationResult mutation) {
        return new PRMutant(
                false,
                mutation.getDetails().getLineNumber(),
                mutation.getDetails().getDescription(),
                mutation.getDetails().getMutator()
        );
    }
}
