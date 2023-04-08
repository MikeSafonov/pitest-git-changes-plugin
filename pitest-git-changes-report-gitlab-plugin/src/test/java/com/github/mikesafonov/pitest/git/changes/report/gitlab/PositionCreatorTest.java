package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.SourcePathResolver;

import static org.mockito.Mockito.mock;

public class PositionCreatorTest {
    private final SourcePathResolver pathResolver = mock(SourcePathResolver.class);
    private final PositionCreator positionCreator = new PositionCreator(pathResolver);
}
