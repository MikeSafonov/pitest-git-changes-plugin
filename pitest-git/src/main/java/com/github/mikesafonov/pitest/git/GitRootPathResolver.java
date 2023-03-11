package com.github.mikesafonov.pitest.git;

import lombok.SneakyThrows;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitRootPathResolver {
    @SneakyThrows
    public Repository resolveRepository(String path) {
        return new FileRepositoryBuilder()
                .setGitDir((path == null) ? null : new File(toGitPath(path)))
                .readEnvironment()
                .findGitDir()
                .build();
    }

    @SneakyThrows
    public Path resolve() {
        try (Repository repository = resolveRepository(null)) {
            return repository.getDirectory().toPath();
        }
    }

    private String toGitPath(String path) {
        if (path.endsWith(".git"))
            return path;
        else
            return Paths.get(path).resolve(".git").toString();
    }
}
