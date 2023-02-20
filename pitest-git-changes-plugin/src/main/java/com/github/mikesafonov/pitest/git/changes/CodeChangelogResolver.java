package com.github.mikesafonov.pitest.git.changes;

import lombok.SneakyThrows;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.pitest.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeChangelogResolver {
    private static final Logger LOGGER = Log.getLogger();

    public CodeChangelog resolve(String repositoryPath, String source, String target) {
        try (Repository repo = findRepository(repositoryPath);
             RevWalk rw = new RevWalk(repo);
             DiffFormatter formatter = createFormatter(repo)) {
            LOGGER.info("Resolving code changes between " + source + " and " + target + " branches");

            RevCommit commit = rw.parseCommit(repo.resolve(source));
            RevCommit parent = rw.parseCommit(repo.resolve(target));

            List<CodeChange> codeChanges = formatter.scan(parent.getTree(), commit.getTree()).stream()
                    .flatMap(entry -> toChangedCode(formatter, entry))
                    .collect(Collectors.toList());
            LOGGER.info("Found " + codeChanges.size() + " changes: ");

            if (LOGGER.isLoggable(Level.FINE)) {
                for (CodeChange codeChange : codeChanges) {
                    LOGGER.fine(codeChange.toString());
                }
            }
            return new CodeChangelog(codeChanges);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            return new CodeChangelog(Collections.emptyList());
        }
    }

    @SneakyThrows
    private Repository findRepository(String path) {
        String gitPath = path;
        if (!path.endsWith(".git")) {
            gitPath = Paths.get(path).resolve(".git").toString();
        }
        return new FileRepositoryBuilder()
                .setGitDir(new File(gitPath))
                .readEnvironment()
                .findGitDir()
                .build();
    }

    private DiffFormatter createFormatter(Repository repository) {
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(repository);
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);
        formatter.setContext(0);
        return formatter;
    }

    private Stream<CodeChange> toChangedCode(DiffFormatter formatter, DiffEntry diffEntry) {
        try {
            FileHeader header = formatter.toFileHeader(diffEntry);
            return header.toEditList().stream()
                    .filter(CodeChangelogResolver::isInsertOrReplace)
                    .map(edit -> new CodeChange(diffEntry.getNewPath(), edit.getBeginB() + 1, edit.getEndB()));
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
