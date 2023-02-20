package com.github.mikesafonov.pitest.git.changes;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeChangelogResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeChangelogResolver.class);

    public CodeChangelog resolve(String repositoryPath, String source, String target) {
        try (FileRepository repo = new FileRepository(new File(repositoryPath));
             RevWalk rw = new RevWalk(repo);
             DiffFormatter formatter = createFormatter(repo)) {
            LOGGER.info("Resolving code changes between " + source + " and " + target + " branches");

            RevCommit commit = rw.parseCommit(repo.resolve(source));
            RevCommit parent = rw.parseCommit(repo.resolve(target));

            List<CodeChange> codeChanges = formatter.scan(parent.getTree(), commit.getTree()).stream()
                    .flatMap(entry -> toChangedCode(formatter, entry))
                    .collect(Collectors.toList());
            LOGGER.info("Found " + codeChanges.size() + " changes: ");
            if (LOGGER.isDebugEnabled()) {
                for (CodeChange codeChange : codeChanges) {
                    LOGGER.debug(codeChange.toString());
                }
            }
            return new CodeChangelog(codeChanges);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new CodeChangelog(Collections.emptyList());
        }
    }

    private DiffFormatter createFormatter(FileRepository repository) {
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
            LOGGER.error(e.getMessage(), e);
            return Stream.empty();
        }
    }

    private static boolean isInsertOrReplace(Edit edit) {
        Edit.Type type = edit.getType();
        return type == Edit.Type.INSERT || type == Edit.Type.REPLACE;
    }
}
