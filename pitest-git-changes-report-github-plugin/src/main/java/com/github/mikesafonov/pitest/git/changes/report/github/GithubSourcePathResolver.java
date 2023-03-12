package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.GitRootPathResolver;
import org.pitest.util.Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class GithubSourcePathResolver {
    private static final Logger LOGGER = Log.getLogger();
    private final Path gitRoot;
    private final List<Path> sourcePaths;

    public GithubSourcePathResolver(Path gitRoot, Collection<Path> originalSourcePaths) {
        this.gitRoot = gitRoot;
        this.sourcePaths = new ArrayList<>(originalSourcePaths);
    }

    public GithubSourcePathResolver(Collection<Path> originalSourcePaths) {
        this(new GitRootPathResolver().resolve().getParent(), originalSourcePaths);
    }

    public String getPath(String name) {
        LOGGER.fine("git root path " + gitRoot.toString());
        for (Path sourcePath : sourcePaths) {
            LOGGER.fine("source paths: " + sourcePath.toString());
        }
        return sourcePaths.stream()
                .map(path -> path.resolve(name))
                .filter(path -> path.toFile().exists())
                .findFirst()
                .map(gitRoot::relativize)
                .map(Path::normalize)
                .map(Path::toString)
                .orElse(null);
    }
}
