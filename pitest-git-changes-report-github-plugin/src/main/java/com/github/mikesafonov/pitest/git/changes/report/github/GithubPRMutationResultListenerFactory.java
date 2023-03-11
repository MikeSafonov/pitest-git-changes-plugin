package com.github.mikesafonov.pitest.git.changes.report.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikesafonov.pitest.git.changes.report.PRMutationResultListenerFactory;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;
import lombok.SneakyThrows;
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
                        new GithubSourcePathResolver(args.data().getSourcePaths()),
                        props.getProperty("PROJECT_NAME")
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
