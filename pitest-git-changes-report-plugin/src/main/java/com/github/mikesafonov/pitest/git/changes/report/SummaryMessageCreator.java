package com.github.mikesafonov.pitest.git.changes.report;

import java.util.List;
import java.util.Map;

import static com.github.mikesafonov.pitest.git.changes.report.Emoji.*;

public class SummaryMessageCreator {

    public String create(PRReport report, String projectName) {
        if (report.getTotal() == 0) {
            return "Pitest mutation report summary. No mutations was found. " + EYES_EMOJI;
        }
        String header = (projectName == null) ? "Pitest mutation report summary." :
                String.format("Pitest mutation report summary for project **%s**", projectName);
        StringBuilder builder = new StringBuilder(header)
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
