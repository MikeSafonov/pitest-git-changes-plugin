package com.github.mikesafonov.pitest.git.changes;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class GitChangesMutationInterceptor implements MutationInterceptor {

    private final CodeChangelog changelog;

    public GitChangesMutationInterceptor(CodeChangelog changelog) {
        this.changelog = changelog;
    }

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {

    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m) {
        return mutations.stream()
                .filter(mutation -> changelog.contains(mutation.getId().getClassName().toString(), mutation.getLineNumber()))
                .collect(Collectors.toList());
    }

    @Override
    public void end() {

    }
}
