package com.github.mikesafonov.pitest.git.changes;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;

import java.nio.file.Paths;

public class GitChangesMutationInterceptorFactory implements MutationInterceptorFactory {
    private static final FeatureParameter SOURCE_PARAMETER = FeatureParameter.named("source")
            .withDescription("Source git branch");
    private static final FeatureParameter TARGET_PARAMETER = FeatureParameter.named("target")
            .withDescription("Target git branch analyse to. Default 'master'");
    private static final FeatureParameter GIT_REPOSITORY_PATH = FeatureParameter.named("repository")
            .withDescription("Path to git repository root folder. Default project folder");

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        String source = params.getString(SOURCE_PARAMETER).orElseThrow(() -> new RuntimeException("Unable to find 'source' parameter"));
        String target = params.getString(TARGET_PARAMETER).orElse("master");
        String repository = params.getString(GIT_REPOSITORY_PATH).orElseGet(() -> Paths.get("").toAbsolutePath().toString());

        CodeChangelogResolver resolver = new CodeChangelogResolver();
        CodeChangelog changelog = resolver.resolve(repository, source, target, mapper(params));

        return new GitChangesMutationInterceptor(changelog);
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

    private static ToMutationClassPathClassNameFunction mapper(InterceptorParameters params) {
        return new ToMutationClassPathClassNameFunction(
                params.data().getClassPath().findClasses(params.data().getTargetClassesFilter())
        );
    }
}
