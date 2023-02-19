package com.github.mikesafonov.pitest.git.changes;

import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class TestGit implements AutoCloseable {
    private final File repository;
    private final Git git;

    @SneakyThrows
    public TestGit(File repository) {
        this.repository = repository;
        this.git = Git.init().setDirectory(repository).call();
    }

    @SneakyThrows
    public void commit(String message) {
        git.add().addFilepattern(".").call();
        git.commit().setAll(true).setMessage(message).call();
    }

    @SneakyThrows
    public void checkout(String branch, boolean createBranch) {
        git.checkout().setCreateBranch(createBranch).setName(branch).call();
    }

    @Override
    public void close() {
        git.close();
    }
}
