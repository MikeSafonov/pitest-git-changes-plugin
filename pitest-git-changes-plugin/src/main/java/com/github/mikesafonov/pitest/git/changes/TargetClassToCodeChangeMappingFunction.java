package com.github.mikesafonov.pitest.git.changes;

import com.github.mikesafonov.pitest.git.GitChange;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@RequiredArgsConstructor
public class TargetClassToCodeChangeMappingFunction implements Function<GitChange, Optional<CodeChange>> {
    private final Set<String> targetClasses;

    @Override
    public Optional<CodeChange> apply(GitChange change) {
        String className = toClassName(change.getFileName());
        return targetClasses.stream()
                .filter(className::endsWith)
                .map(s -> new CodeChange(s, change.getLineFrom(), change.getLineTo()))
                .findFirst();
    }

    private static String toClassName(String filename) {
        String name = FilenameUtils.removeExtension(filename);
        return name.replace("/", ".");
    }
}
