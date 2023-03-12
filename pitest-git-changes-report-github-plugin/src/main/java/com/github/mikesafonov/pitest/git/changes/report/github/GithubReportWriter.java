package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.changes.report.MutatedClass;
import com.github.mikesafonov.pitest.git.changes.report.PRMutant;
import com.github.mikesafonov.pitest.git.changes.report.PRReport;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;
import lombok.SneakyThrows;
import lombok.Value;
import org.kohsuke.github.*;
import org.pitest.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Value
public class GithubReportWriter implements ReportWriter {
    private static final Logger LOGGER = Log.getLogger();
    private static final String PITEST_CHECKS_NAME = "pitest-checks";
    private String token;
    private long repoId;
    private int prId;
    private String sha;
    private GithubSourcePathResolver pathResolver;
    private String projectName;

    private final GithubMessageCreator messageCreator = new GithubMessageCreator();

    @Override
    @SneakyThrows
    public void write(PRReport report) {
        GHRepository repository = getRepository();
        String summary = messageCreator.create(report);
        GHPullRequest pullRequest = repository.getPullRequest(prId);
        pullRequest.comment(summary);
        publishChecks(repository, report, summary);
    }

    @SneakyThrows
    private GHRepository getRepository() {
        return GitHub.connectUsingOAuth(token).getRepositoryById(repoId);
    }

    @SneakyThrows
    private void publishChecks(GHRepository repository, PRReport report, String summary) {
        GHCheckRunBuilder checkRunBuilder = repository.createCheckRun(createCheckName(), sha)
                .withStatus(GHCheckRun.Status.COMPLETED);
        List<GHCheckRunBuilder.Annotation> annotations = createAnnotations(report);
        if (annotations.isEmpty()) {
            checkRunBuilder.withConclusion(GHCheckRun.Conclusion.SUCCESS);
        } else {
            GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Pitest report", summary);
            annotations.forEach(output::add);
            checkRunBuilder.withConclusion(GHCheckRun.Conclusion.FAILURE)
                    .add(output);
        }

        checkRunBuilder.create();
    }

    private String createCheckName() {
        return (projectName == null) ? PITEST_CHECKS_NAME : PITEST_CHECKS_NAME + "-" + projectName;
    }

    private List<GHCheckRunBuilder.Annotation> createAnnotations(PRReport report) {
        List<GHCheckRunBuilder.Annotation> annotations = new ArrayList<>();
        for (Map.Entry<MutatedClass, List<PRMutant>> entry : report.getMutants().entrySet()) {
            String classPath = getPath(entry.getKey());
            entry.getValue().stream()
                    .filter(PRMutant::isSurvived)
                    .map(mutant -> createAnnotation(classPath, mutant))
                    .forEach(annotations::add);
        }
        return annotations;
    }

    private static GHCheckRunBuilder.Annotation createAnnotation(String classPath, PRMutant mutant) {
        return new GHCheckRunBuilder.Annotation(
                classPath,
                mutant.getLineNumber(),
                GHCheckRun.AnnotationLevel.FAILURE,
                mutant.getDescription()
        ).withTitle(mutant.getMutator());
    }

    private String getPath(MutatedClass mutatedClass) {
        String name = mutatedClass.toRealName();
        String path = pathResolver.getPath(name);
        LOGGER.fine("resolved path of " + name + " path = " + path);
        return (path == null) ? name : path;
    }
}
