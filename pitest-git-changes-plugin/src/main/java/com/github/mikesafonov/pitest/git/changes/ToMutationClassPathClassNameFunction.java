package com.github.mikesafonov.pitest.git.changes;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class ToMutationClassPathClassNameFunction implements Function<CodeChange, Optional<CodeChange>> {
    private final Collection<String> targetClasses;

    @Override
    public Optional<CodeChange> apply(CodeChange change) {
        return targetClasses.stream()
                .filter(s -> change.getClassName().endsWith(s))
                .map(s -> new CodeChange(s, change.getLineFrom(), change.getLineTo()))
                .findFirst();
    }
}
