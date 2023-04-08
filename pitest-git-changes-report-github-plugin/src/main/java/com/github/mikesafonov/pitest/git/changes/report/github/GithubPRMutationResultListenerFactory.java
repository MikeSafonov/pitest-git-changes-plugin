package com.github.mikesafonov.pitest.git.changes.report.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikesafonov.pitest.git.changes.report.PRMutationResultListenerFactory;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;
import com.github.mikesafonov.pitest.git.changes.report.SourcePathResolver;
import lombok.SneakyThrows;
import org.kohsuke.github.GHCheckRun;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.util.Log;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;


public class GithubPRMutationResultListenerFactory extends PRMutationResultListenerFactory {
    private static final Logger LOGGER = Log.getLogger();

    @Override
    protected Optional<ReportWriter> getWriter(Properties props, ListenerArguments args) {
        String token = props.getProperty("GITHUB_TOKEN");
        String repoId = props.getProperty("GITHUB_REPOSITORY_ID");
        GHCheckRun.AnnotationLevel survivedLevel = getSurvivedLevel(props);
        boolean failIfMutantsPresent = getFailIfMutantsPresent(props);
        if (repoId != null) {
            JsonNode pullRequestJson = readPullRequestJson(props);
            if (pullRequestJson != null) {
                String sha = pullRequestJson.get("head").get("sha").asText();
                int pullRequestId = Integer.parseInt(pullRequestJson.get("number").asText());
                return Optional.of(new GithubReportWriter(
                        token,
                        Long.parseLong(repoId),
                        pullRequestId,
                        sha,
                        SourcePathResolver.withGitResolver(args.data().getSourcePaths()),
                        props.getProperty("PROJECT_NAME"),
                        survivedLevel,
                        failIfMutantsPresent
                ));
            }
        }
        LOGGER.info("Unable to create GithubReportWriter. Please verify GITHUB_REPOSITORY_ID, GITHUB_TOKEN and GITHUB_EVENT_PATH envs passed. Fallback to default ReportWriter");
        return Optional.empty();
    }

    @Override
    public String name() {
        return "GITHUB";
    }

    @Override
    public String description() {
        return "Sending mutation report to Github Pull Request";
    }

    private GHCheckRun.AnnotationLevel getSurvivedLevel(Properties props) {
        String level = props.getProperty("GITHUB_MUTANT_LEVEL");
        if (level == null) {
            return GHCheckRun.AnnotationLevel.FAILURE;
        }
        return GHCheckRun.AnnotationLevel.valueOf(level);
    }

    private boolean getFailIfMutantsPresent(Properties props) {
        String githubFailIfMutantsPresent = props.getProperty("GITHUB_FAIL_IF_MUTANTS_PRESENT");
        if(githubFailIfMutantsPresent == null) {
            return true;
        }
        return Boolean.getBoolean(githubFailIfMutantsPresent);
    }

    @SneakyThrows
    private JsonNode readPullRequestJson(Properties props) {
        String eventPath = props.getProperty("GITHUB_EVENT_PATH");
        if (eventPath == null) {
            return null;
        }
        try (InputStream fileStream = Files.newInputStream(Paths.get(eventPath))) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(fileStream);
            return jsonNode.get("pull_request");
        }
    }
}
