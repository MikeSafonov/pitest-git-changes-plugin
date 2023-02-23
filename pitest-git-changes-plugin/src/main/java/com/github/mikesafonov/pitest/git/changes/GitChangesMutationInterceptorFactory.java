package com.github.mikesafonov.pitest.git.changes;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;
import org.pitest.util.Log;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GitChangesMutationInterceptorFactory implements MutationInterceptorFactory {
    private static final Logger LOGGER = Log.getLogger();
    private static final FeatureParameter SOURCE_PARAMETER = FeatureParameter.named("source")
            .withDescription("Source git branch");
    private static final FeatureParameter TARGET_PARAMETER = FeatureParameter.named("target")
            .withDescription("Target git branch analyse to. Default 'master'");
    private static final FeatureParameter GIT_REPOSITORY_PATH = FeatureParameter.named("repository")
            .withDescription("Optional path to git repository directory. If not present searching git repository starting from project directory");

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        String source = params.getString(SOURCE_PARAMETER).orElseThrow(() -> new RuntimeException("Unable to find 'source' parameter"));
        String target = params.getString(TARGET_PARAMETER).orElse("master");
        String repository = params.getString(GIT_REPOSITORY_PATH).orElse(null);

        GitChangeResolver gitChangeResolver = new GitChangeResolver();
        List<CodeChange> changes = gitChangeResolver.resolve(repository, source, target)
                .map(mapper(params))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        logChanges(changes);
        return new GitChangesMutationInterceptor(new CodeChangelog(changes));
    }

    @Override
    public Feature provides() {
        return Feature.named("git-changes")
                .withDescription(description())
                .withParameter(SOURCE_PARAMETER)
                .withParameter(TARGET_PARAMETER)
                .withParameter(GIT_REPOSITORY_PATH);
    }

    @Override
    public String description() {
        return "git changes plugin";
    }

    private static TargetClassToCodeChangeMappingFunction mapper(InterceptorParameters params) {
        return new TargetClassToCodeChangeMappingFunction(
                params.data().getClassPath().findClasses(params.data().getTargetClassesFilter())
        );
    }

    private void logChanges(List<CodeChange> changes) {
        LOGGER.info("Found " + changes.size() + " changes ");

        if (LOGGER.isLoggable(Level.FINE)) {
            for (CodeChange change : changes) {
                LOGGER.fine(change.toString());
            }
        }
    }
}
