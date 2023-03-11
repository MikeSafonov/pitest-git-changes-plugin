package com.github.mikesafonov.pitest.git;

import lombok.SneakyThrows;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.pitest.util.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class GitChangeResolver {
    private static final Logger LOGGER = Log.getLogger();

    private final GitRootPathResolver gitRootPathResolver = new GitRootPathResolver();

    public Stream<GitChange> resolve(String repositoryPath, String source, String target) {
        try (Repository repo = gitRootPathResolver.resolveRepository(repositoryPath);
             RevWalk rw = new RevWalk(repo);
             DiffFormatter formatter = createFormatter(repo)) {
            LOGGER.info("Resolving code changes between " + ((source == null) ? "local" : source) + " and " + target + " branches");

            return formatter.scan(
                            targetTreeIterator(target, rw, repo),
                            sourceTreeIterator(source, rw, repo)
                    )
                    .stream()
                    .flatMap(entry -> toGitChange(formatter, entry));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            return Stream.empty();
        }
    }

    private AbstractTreeIterator sourceTreeIterator(String source, RevWalk rw, Repository repo) {
        if (source == null) {
            return new FileTreeIterator(repo);
        }
        return getTreeIterator(source, rw, repo);
    }

    private AbstractTreeIterator targetTreeIterator(String target, RevWalk rw, Repository repo) {
        return getTreeIterator(target, rw, repo);
    }

    @SneakyThrows
    private AbstractTreeIterator getTreeIterator(String rev, RevWalk rw, Repository repo) {
        RevCommit commit = rw.parseCommit(repo.resolve(rev));
        RevTree tree = commit.getTree();
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (ObjectReader reader = repo.newObjectReader()) {
            treeParser.reset(reader, tree.getId());
        }
        return treeParser;
    }

    private DiffFormatter createFormatter(Repository repository) {
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(repository);
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);
        formatter.setContext(0);
        return formatter;
    }

    private Stream<GitChange> toGitChange(DiffFormatter formatter, DiffEntry diffEntry) {
        try {
            FileHeader header = formatter.toFileHeader(diffEntry);
            return header.toEditList().stream()
                    .filter(GitChangeResolver::isInsertOrReplace)
                    .map(edit -> new GitChange(diffEntry.getNewPath(), edit.getBeginB() + 1, edit.getEndB()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            return Stream.empty();
        }
    }

    private static boolean isInsertOrReplace(Edit edit) {
        Edit.Type type = edit.getType();
        return type == Edit.Type.INSERT || type == Edit.Type.REPLACE;
    }
}
