package com.github.mikesafonov.pitest.git.changes.report.gitlab;

import com.github.mikesafonov.pitest.git.changes.report.PRMutationResultListenerFactory;
import com.github.mikesafonov.pitest.git.changes.report.ReportWriter;
import com.github.mikesafonov.pitest.git.changes.report.SourcePathResolver;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.util.Log;

import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class GitlabPRMutationResultListenerFactory extends PRMutationResultListenerFactory {
    private static final Logger LOGGER = Log.getLogger();

    @Override
    protected Optional<ReportWriter> getWriter(Properties props, ListenerArguments args) {
        String gitlabUrl = props.getProperty("GITLAB_URL");
        String token = props.getProperty("GITLAB_TOKEN");
        String projectId = props.getProperty("GITLAB_PROJECT_ID");
        String projectName = props.getProperty("PROJECT_NAME");

        if (gitlabUrl != null && token != null && projectId != null) {
            Long mrId = getMergeRequestId(props);
            return Optional.of(
                    new GitlabReportWriter(
                            gitlabUrl,
                            token,
                            projectId,
                            mrId,
                            SourcePathResolver.withGitResolver(args.data().getSourcePaths()),
                            projectName
                    )
            );
        }
        LOGGER.info("Unable to create GitlabReportWriter. Please verify GITLAB_URL, GITLAB_TOKEN, GITLAB_PROJECT_ID and GITLAB_MR_ID envs passed. Fallback to default ReportWriter");

        return Optional.empty();
    }

    private Long getMergeRequestId(Properties props) {
        String gitlabMrId = props.getProperty("GITLAB_MR_ID");
        try {
            return Long.parseLong(gitlabMrId);
        } catch (NumberFormatException e) {
            return Long.parseLong(gitlabMrId.split("!")[1]);
        }
    }


    @Override
    public String name() {
        return "GITLAB";
    }

    @Override
    public String description() {
        return "Sending mutation report to Gitlab Merge Request";
    }
}
