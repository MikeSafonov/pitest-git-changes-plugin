package com.github.mikesafonov.pitest.git.changes;

import lombok.Value;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangelogCreator {
    private static final Logger LOGGER = Log.getLogger();
    private static final ChangelogCreator INSTANCE = new ChangelogCreator();

    private final Map<SourceKey, CodeChangelog> cache;

    private ChangelogCreator() {
        cache = new ConcurrentHashMap<>();
    }

    public static ChangelogCreator getInstance() {
        return INSTANCE;
    }

    public CodeChangelog create(String repositoryPath, String source, String target, InterceptorParameters params) {
        SourceKey key = new SourceKey(repositoryPath, source, target);
        return cache.computeIfAbsent(key, sourceKey -> createCodeChangelog(repositoryPath, source, target, params));
    }

    private CodeChangelog createCodeChangelog(String repositoryPath, String source, String target, InterceptorParameters params) {
        List<CodeChange> changes = new GitChangeResolver().resolve(repositoryPath, source, target)
                .map(mapper(params))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        logChanges(changes);
        return new CodeChangelog(changes);
    }

    private static TargetClassToCodeChangeMappingFunction mapper(InterceptorParameters params) {
        return new TargetClassToCodeChangeMappingFunction(
                collectTargetClassesAndTests(params)
        );
    }

    private static Set<String> collectTargetClassesAndTests(InterceptorParameters params) {
        ReportOptions data = params.data();
        ClassPath classPath = data.getClassPath();
        return Stream.concat(
                        classPath.findClasses(data.getTargetClassesFilter()).stream(),
                        classPath.findClasses(data.getTargetTestsFilter()).stream()
                )
                .collect(Collectors.toSet());
    }

    private void logChanges(List<CodeChange> changes) {
        LOGGER.info("Found " + changes.size() + " changes ");

        if (LOGGER.isLoggable(Level.FINE)) {
            for (CodeChange change : changes) {
                LOGGER.fine(change.toString());
            }
        }
    }

    @Value
    private static class SourceKey {
        private String repositoryPath;
        private String source;
        private String target;
    }
}
