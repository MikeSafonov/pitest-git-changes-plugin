package com.github.mikesafonov.pitest.git.changes;

import org.eclipse.jgit.lib.Constants;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;


public class GitChangesMutationInterceptorFactory implements MutationInterceptorFactory {
    private static final FeatureParameter SOURCE_PARAMETER = FeatureParameter.named("source")
            .withDescription("Source git branch(Optional). Default using local changes");
    private static final FeatureParameter TARGET_PARAMETER = FeatureParameter.named("target")
            .withDescription("Target git branch analyse to(Optional). Default 'HEAD'");
    private static final FeatureParameter GIT_REPOSITORY_PATH = FeatureParameter.named("repository")
            .withDescription("Path to git repository directory(Optional). If not present searching git repository starting from project directory");

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        String source = params.getString(SOURCE_PARAMETER).orElse(null);
        String target = params.getString(TARGET_PARAMETER).orElse(Constants.HEAD);
        String repository = params.getString(GIT_REPOSITORY_PATH).orElse(null);
        return new GitChangesMutationInterceptor(ChangelogCreator.getInstance().create(repository, source, target, params));
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
}
