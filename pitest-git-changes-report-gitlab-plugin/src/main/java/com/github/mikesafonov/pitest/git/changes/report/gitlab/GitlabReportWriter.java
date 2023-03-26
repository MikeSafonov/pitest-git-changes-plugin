package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.*;
import lombok.SneakyThrows;
import lombok.Value;
import org.gitlab4j.api.DiscussionsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.models.*;
import org.pitest.util.Log;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Value
public class GitlabReportWriter implements ReportWriter {
    private static final Logger LOGGER = Log.getLogger();

    private static final String PITEST_REPORT_TAG_PREFIX = "### PITEST REPORT";

    private final String gitlabHost;
    private final String token;
    private final String projectId;
    private final Long mergeRequestId;

    private final SourcePathResolver pathResolver;

    private final String projectName;

    private final SummaryMessageCreator messageCreator = new SummaryMessageCreator();

    @Override
    @SneakyThrows
    public void write(PRReport report) {
        try (GitLabApi gitLabApi = new GitLabApi(gitlabHost, token)) {
            String summary = messageCreator.create(report, projectName);
            gitLabApi.getNotesApi().createMergeRequestNote(projectId, mergeRequestId, summary);
            DiscussionsApi discussionsApi = gitLabApi.getDiscussionsApi();
            resolvePreviousDiscussions(discussionsApi);
            if (report.getSurvived() > 0) {
                MergeRequestApi mergeRequestApi = gitLabApi.getMergeRequestApi();
                MergeRequest mergeRequest = mergeRequestApi.getMergeRequest(projectId, mergeRequestId);
                DiffRef diffRefs = mergeRequest.getDiffRefs();
                createDiscussions(report, diffRefs, discussionsApi);
            }
        }
    }

    @SneakyThrows
    private void resolvePreviousDiscussions(DiscussionsApi discussionsApi) {
        List<Discussion> discussions = discussionsApi.getMergeRequestDiscussions(projectId, mergeRequestId);
        for (Discussion discussion : discussions) {
            for (Note note : discussion.getNotes()) {
                if (note.getResolvable() && !note.getResolved() && isPitestBody(note.getBody())) {
                    discussionsApi.resolveMergeRequestDiscussion(projectId, mergeRequestId, discussion.getId(), true);
                }
            }
        }
    }

    private void createDiscussions(PRReport report, DiffRef diffRefs, DiscussionsApi discussionsApi) throws GitLabApiException {
        for (Map.Entry<MutatedClass, List<PRMutant>> entry : report.getMutants().entrySet()) {
            MutatedClass mutatedClass = entry.getKey();
            for (PRMutant mutant : entry.getValue().stream().filter(PRMutant::isSurvived).collect(Collectors.toList())) {
                discussionsApi.createMergeRequestDiscussion(
                        projectId,
                        mergeRequestId,
                        mutantToBody(mutant),
                        null,
                        null,
                        createPosition(mutatedClass, mutant, diffRefs)
                );
            }
        }
    }

    private boolean isPitestBody(String body) {
        return body.startsWith(getProjectTag());
    }

    private Position createPosition(MutatedClass mutatedClass, PRMutant mutant, DiffRef diffRef) {
        return new Position()
                .withBaseSha(diffRef.getBaseSha())
                .withHeadSha(diffRef.getHeadSha())
                .withPositionType(Position.PositionType.TEXT)
                .withStartSha(diffRef.getStartSha())
                .withNewLine(mutant.getLineNumber())
                .withNewPath(getPath(mutatedClass));

    }

    private String getProjectTag() {
        if (projectName == null) {
            return PITEST_REPORT_TAG_PREFIX;
        }
        return PITEST_REPORT_TAG_PREFIX + " (" + projectName + ")";
    }

    private String mutantToBody(PRMutant mutant) {
        return getProjectTag() + "\n**" + mutant.getMutator() + "**\n\n" + mutant.getDescription() + " (line " + mutant.getLineNumber() + ")";
    }

    private String getPath(MutatedClass mutatedClass) {
        String name = mutatedClass.toRealName();
        String path = pathResolver.getPath(name);
        LOGGER.fine("resolved path of " + name + " path = " + path);
        return (path == null) ? name : path;
    }
}
