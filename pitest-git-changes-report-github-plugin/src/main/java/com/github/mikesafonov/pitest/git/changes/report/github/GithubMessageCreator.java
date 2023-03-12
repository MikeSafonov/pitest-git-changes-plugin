package com.github.mikesafonov.pitest.git.changes.report.github;

import com.github.mikesafonov.pitest.git.changes.report.MutatedClass;
import com.github.mikesafonov.pitest.git.changes.report.PRMutant;
import com.github.mikesafonov.pitest.git.changes.report.PRReport;

import java.util.List;
import java.util.Map;

public class GithubMessageCreator {
    private static final String EYES_EMOJI = "\uD83D\uDC40";
    private static final String GOOD_EMOJI = "✅";
    private static final String MUTANTS_EMOJI = "\uD83D\uDC1B";

    public String create(PRReport report) {
        if (report.getTotal() == 0) {
            return "Pitest mutation report summary. No mutations was found. " + EYES_EMOJI;
        }
        StringBuilder builder = new StringBuilder("Pitest mutation report summary.")
                .append("\n`Total mutants:` ")
                .append(report.getTotal())
                .append("\n`Killed mutants:` ")
                .append(report.getKilled())
                .append("\n`Survived mutants:` ")
                .append(report.getSurvived());
        builder.append("\n| Class  | Survived | Killed |");
        builder.append("\n| ------------- | ------------- | ------------- |");
        for (Map.Entry<MutatedClass, List<PRMutant>> entry : report.getMutants().entrySet()) {
            MutatedClass mutatedClass = entry.getKey();
            long survived = entry.getValue().stream().filter(PRMutant::isSurvived).count();
            long killed = entry.getValue().size() - survived;
            String classEmoji = (survived == 0) ? GOOD_EMOJI : MUTANTS_EMOJI;
            builder.append("\n| " + classEmoji + " " + mutatedClass.getRelativePath() + " | " + survived + " | " + killed + " |");
        }

        return builder.toString();
    }
}
