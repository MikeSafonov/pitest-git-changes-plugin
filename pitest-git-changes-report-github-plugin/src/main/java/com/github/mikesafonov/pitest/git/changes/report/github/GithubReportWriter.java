package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.changes.report.*;
import lombok.SneakyThrows;
import org.kohsuke.github.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mikesafonov.pitest.git.changes.report.Emoji.GOOD_EMOJI;

public class GithubReportWriter implements ReportWriter {
    private static final String PITEST_CHECKS_NAME = "pitest-checks";
    private final GHRepository repository;
    private final int prId;
    private final String sha;
    private final SourcePathResolver pathResolver;
    private final String projectName;
    private final GHCheckRun.AnnotationLevel survivedLevel;
    private final boolean failIfMutantsPresent;

    private final SummaryMessageCreator messageCreator;

    public GithubReportWriter(String token,
                              long repoId,
                              int prId,
                              String sha,
                              SourcePathResolver pathResolver,
                              String projectName,
                              GHCheckRun.AnnotationLevel survivedLevel,
                              boolean failIfMutantsPresent) {
        this(
                createRepository(token, repoId),
                prId,
                sha,
                pathResolver,
                projectName,
                survivedLevel,
                failIfMutantsPresent,
                new SummaryMessageCreator()
        );
    }

    public GithubReportWriter(GHRepository repository,
                              int prId,
                              String sha,
                              SourcePathResolver pathResolver,
                              String projectName,
                              GHCheckRun.AnnotationLevel survivedLevel,
                              boolean failIfMutantsPresent,
                              SummaryMessageCreator summaryMessageCreator) {
        this.repository = repository;
        this.prId = prId;
        this.sha = sha;
        this.pathResolver = pathResolver;
        this.projectName = projectName;
        this.survivedLevel = survivedLevel;
        this.failIfMutantsPresent = failIfMutantsPresent;
        this.messageCreator = summaryMessageCreator;
    }

    @SneakyThrows
    private static GHRepository createRepository(String token,
                                          long repoId) {
        return GitHub.connectUsingOAuth(token).getRepositoryById(repoId);
    }


    @Override
    @SneakyThrows
    public void write(PRReport report) {
        String summary = messageCreator.create(report, projectName);
        GHPullRequest pullRequest = repository.getPullRequest(prId);
        pullRequest.comment(summary);
        publishChecks(report, summary);
    }

    @SneakyThrows
    private void publishChecks(PRReport report, String summary) {
        GHCheckRunBuilder checkRunBuilder = repository.createCheckRun(createCheckName(), sha)
                .withStatus(GHCheckRun.Status.COMPLETED);
        List<GHCheckRunBuilder.Annotation> annotations = createAnnotations(report);
        GHCheckRun.Conclusion conclusion = getConclusion(report);

        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Pitest report", summary);
        annotations.forEach(output::add);
        checkRunBuilder.withConclusion(conclusion)
                .add(output)
                .create();
    }

    private String createCheckName() {
        return (projectName == null) ? PITEST_CHECKS_NAME : PITEST_CHECKS_NAME + "-" + projectName;
    }

    private List<GHCheckRunBuilder.Annotation> createAnnotations(PRReport report) {
        List<GHCheckRunBuilder.Annotation> annotations = new ArrayList<>();
        for (Map.Entry<MutatedClass, List<PRMutant>> entry : report.getMutants().entrySet()) {
            String classPath = pathResolver.getPath(entry.getKey());
            entry.getValue().stream()
                    .map(mutant -> createAnnotation(classPath, mutant))
                    .forEach(annotations::add);
        }
        return annotations;
    }

    private GHCheckRunBuilder.Annotation createAnnotation(String classPath, PRMutant mutant) {
        GHCheckRun.AnnotationLevel level;
        String title;
        if (mutant.isSurvived()) {
            level = survivedLevel;
            title = mutant.getMutator();
        } else {
            level = GHCheckRun.AnnotationLevel.NOTICE;
            title = GOOD_EMOJI + " " + mutant.getMutator();
        }
        return new GHCheckRunBuilder.Annotation(
                classPath,
                mutant.getLineNumber(),
                level,
                mutant.getDescription()
        ).withTitle(title);
    }

    private GHCheckRun.Conclusion getConclusion(PRReport report) {
        if (report.isFullyKilled() || !failIfMutantsPresent) {
            return GHCheckRun.Conclusion.SUCCESS;
        }
        return GHCheckRun.Conclusion.FAILURE;
    }

    @Override
    public void close() {

    }
}
