package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.*;
import lombok.SneakyThrows;
import org.gitlab4j.api.DiscussionsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.models.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitlabReportWriter implements ReportWriter {
    private static final String PITEST_REPORT_TAG_PREFIX = "### PITEST REPORT";

    private final GitLabApi gitLabApi;
    private final String projectId;
    private final Long mergeRequestId;

    private final PositionCreator positionCreator;

    private final String projectName;

    private final SummaryMessageCreator messageCreator;

    public GitlabReportWriter(
            String gitlabHost,
            String token,
            String projectId,
            Long mergeRequestId,
            SourcePathResolver pathResolver,
            String projectName
    ) {
        this(
                new GitLabApi(gitlabHost, token),
                projectId,
                mergeRequestId,
                pathResolver,
                projectName,
                new SummaryMessageCreator()
        );
    }

    public GitlabReportWriter(
            GitLabApi gitLabApi,
            String projectId,
            Long mergeRequestId,
            SourcePathResolver pathResolver,
            String projectName,
            SummaryMessageCreator messageCreator
    ) {
        this.gitLabApi = gitLabApi;
        this.projectId = projectId;
        this.mergeRequestId = mergeRequestId;
        this.positionCreator = new PositionCreator(pathResolver);
        this.projectName = projectName;
        this.messageCreator = messageCreator;
    }

    public GitlabReportWriter(
            GitLabApi gitLabApi,
            String projectId,
            Long mergeRequestId,
            PositionCreator positionCreator,
            String projectName,
            SummaryMessageCreator messageCreator
    ) {
        this.gitLabApi = gitLabApi;
        this.projectId = projectId;
        this.mergeRequestId = mergeRequestId;
        this.positionCreator = positionCreator;
        this.projectName = projectName;
        this.messageCreator = messageCreator;
    }

    @Override
    @SneakyThrows
    public void write(PRReport report) {
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
            List<PRMutant> survivedMutants = entry.getValue().stream()
                    .filter(PRMutant::isSurvived)
                    .collect(Collectors.toList());
            for (PRMutant mutant : survivedMutants) {
                discussionsApi.createMergeRequestDiscussion(
                        projectId,
                        mergeRequestId,
                        mutantToBody(mutant),
                        null,
                        null,
                        positionCreator.createPosition(mutatedClass, mutant, diffRefs)
                );
            }
        }
    }

    private boolean isPitestBody(String body) {
        return body.startsWith(getProjectTag());
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

    @Override
    public void close() {
        gitLabApi.close();
    }
}
