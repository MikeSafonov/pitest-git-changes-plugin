package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.*;
import lombok.SneakyThrows;
import org.gitlab4j.api.DiscussionsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.NotesApi;
import org.gitlab4j.api.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GitlabReportWriterTest {
    private final GitLabApi gitLabApi = mock(GitLabApi.class);
    private final String projectId = UUID.randomUUID().toString();
    private final Long mergeRequestId = 1L;
    private final PositionCreator positionCreator = mock(PositionCreator.class);
    private final String projectName = UUID.randomUUID().toString();
    private final SummaryMessageCreator messageCreator = mock(SummaryMessageCreator.class);

    private final NotesApi notesApi = mock(NotesApi.class);
    private final DiscussionsApi discussionsApi = mock(DiscussionsApi.class);
    private final MergeRequestApi mergeRequestApi = mock(MergeRequestApi.class);

    private final String summary = UUID.randomUUID().toString();
    private final GitlabReportWriter writer = new GitlabReportWriter(
            gitLabApi,
            projectId,
            mergeRequestId,
            positionCreator,
            projectName,
            messageCreator
    );

    @BeforeEach
    void setUp() {
        when(messageCreator.create(any(), eq(projectName))).thenReturn(summary);
        when(gitLabApi.getNotesApi()).thenReturn(notesApi);
        when(gitLabApi.getDiscussionsApi()).thenReturn(discussionsApi);
        when(gitLabApi.getMergeRequestApi()).thenReturn(mergeRequestApi);
    }

    @Test
    void shouldCloseGitlabApi() {
        writer.close();
        verify(gitLabApi).close();
    }

    @Test
    @SneakyThrows
    void shouldCreateNoteWithSummary() {
        PRReport report = new PRReport(0, 0, 0, new HashMap<>());
        writer.write(report);

        verify(notesApi).createMergeRequestNote(projectId, mergeRequestId, summary);
    }

    @Test
    @SneakyThrows
    void shouldResolveDiscussions() {
        String tag = "### PITEST REPORT (" + projectName + ")";
        List<Discussion> discussionList = Arrays.asList(
                createDiscussion("d1", Arrays.asList(
                        note(true, false, "any"),
                        note(true, false, tag + " any")
                )),
                createDiscussion("d2", Arrays.asList(
                        note(false, false, "any"),
                        note(true, true, "any")
                )),
                createDiscussion("d3", Arrays.asList(
                        note(true, false, tag + " any")
                )),
                createDiscussion("d4", Collections.emptyList())
        );
        when(discussionsApi.getMergeRequestDiscussions(projectId, mergeRequestId))
                .thenReturn(discussionList);
        PRReport report = new PRReport(0, 0, 0, new HashMap<>());
        writer.write(report);


        verify(discussionsApi).getMergeRequestDiscussions(projectId, mergeRequestId);
        verify(discussionsApi).resolveMergeRequestDiscussion(projectId, mergeRequestId, "d1", true);
        verify(discussionsApi).resolveMergeRequestDiscussion(projectId, mergeRequestId, "d3", true);
        verifyNoMoreInteractions(discussionsApi);
    }

    @Test
    @SneakyThrows
    void shouldCreateNewDiscussions() {
        String tag = "### PITEST REPORT (" + projectName + ")";
        MergeRequest mergeRequest = mock(MergeRequest.class);
        DiffRef diffRefs = mock(DiffRef.class);
        when(mergeRequestApi.getMergeRequest(projectId, mergeRequestId)).thenReturn(mergeRequest);
        when(mergeRequest.getDiffRefs()).thenReturn(diffRefs);

        Map<MutatedClass, List<PRMutant>> mutants = new HashMap<>();
        mutants.put(new MutatedClass("test", "java"), Arrays.asList(
                new PRMutant(true, 1, "tt", "mm"),
                new PRMutant(false, 1, "ddd", "sss")
        ));
        mutants.put(new MutatedClass("test2", "java"), Arrays.asList(
                new PRMutant(false, 1, "ddd", "sss"),
                new PRMutant(false, 3, "aa", "hhh"),
                new PRMutant(true, 2, "ddd", "sss")
        ));
        Position position = mock(Position.class);
        when(positionCreator.createPosition(any(), any(), any()))
                .thenReturn(position);

        PRReport report = new PRReport(2, 0, 2, mutants);
        writer.write(report);




        verify(discussionsApi).getMergeRequestDiscussions(projectId, mergeRequestId);
        verify(discussionsApi).createMergeRequestDiscussion(
                eq(projectId),
                eq(mergeRequestId),
                eq("### PITEST REPORT (" + projectName + ")\n" +
                        "**mm**\n" +
                        "\n" +
                        "tt (line 1)"),
                isNull(),
                isNull(),
                eq(position)
        );
        verify(discussionsApi).createMergeRequestDiscussion(
                eq(projectId),
                eq(mergeRequestId),
                eq("### PITEST REPORT (" + projectName + ")\n" +
                        "**sss**\n" +
                        "\n" +
                        "ddd (line 2)"),
                isNull(),
                isNull(),
                eq(position)
        );

        verifyNoMoreInteractions(discussionsApi);
    }

    private Note note(Boolean resolvable, Boolean resolved, String body) {
        Note note = new Note();
        note.setResolvable(resolvable);
        note.setResolved(resolved);
        note.setBody(body);
        return note;
    }

    private Discussion createDiscussion(String id, List<Note> notes) {
        Discussion discussion = new Discussion();
        discussion.setId(id);
        discussion.setNotes(notes);
        return discussion;
    }
}
