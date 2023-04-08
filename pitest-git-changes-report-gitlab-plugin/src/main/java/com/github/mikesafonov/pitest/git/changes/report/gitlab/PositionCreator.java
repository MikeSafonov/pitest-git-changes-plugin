package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.MutatedClass;
import com.github.mikesafonov.pitest.git.changes.report.PRMutant;
import com.github.mikesafonov.pitest.git.changes.report.SourcePathResolver;
import org.gitlab4j.api.models.DiffRef;
import org.gitlab4j.api.models.Position;

public class PositionCreator {
    private final SourcePathResolver pathResolver;

    public PositionCreator(SourcePathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public Position createPosition(MutatedClass mutatedClass, PRMutant mutant, DiffRef diffRef) {
        return new Position()
                .withBaseSha(diffRef.getBaseSha())
                .withHeadSha(diffRef.getHeadSha())
                .withPositionType(Position.PositionType.TEXT)
                .withStartSha(diffRef.getStartSha())
                .withNewLine(mutant.getLineNumber())
                .withNewPath(pathResolver.getPath(mutatedClass));

    }
}
