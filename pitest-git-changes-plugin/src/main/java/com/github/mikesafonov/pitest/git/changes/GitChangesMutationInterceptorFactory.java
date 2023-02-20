package com.github.mikesafonov.pitest.git.changes;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;
import org.pitest.util.Log;

import java.nio.file.Paths;

public class GitChangesMutationInterceptorFactory implements MutationInterceptorFactory {
    private static final FeatureParameter SOURCE_PARAMETER = FeatureParameter.named("source")
            .withDescription("Source git branch");
    private static final FeatureParameter TARGET_PARAMETER = FeatureParameter.named("target")
            .withDescription("Target git branch analyse to. Default 'master'");

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        String source = params.getString(SOURCE_PARAMETER).orElseThrow(() -> new RuntimeException("Unable to find 'source' parameter"));
        String target = params.getString(TARGET_PARAMETER).orElse("master");

        CodeChangelogResolver resolver = new CodeChangelogResolver();
        CodeChangelog changelog = resolver.resolve(Paths.get("").toAbsolutePath().toString(), source, target);

        Log.getLogger().info("Creating git changes interceptor " + this);
        return new GitChangesMutationInterceptor(changelog);
    }

    @Override
    public Feature provides() {
        return Feature.named("git-changes")
                .withDescription(description())
                .withParameter(SOURCE_PARAMETER)
                .withParameter(TARGET_PARAMETER);
    }

    @Override
    public String description() {
        return "git changes plugin";
    }
}
