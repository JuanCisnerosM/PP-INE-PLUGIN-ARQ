package com.miempresa.sonar.rules;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoDomainAccessFromExpositionRule implements Sensor {

    private static final RuleKey RULE_KEY = RuleKey.of("arq-rules-plugin", "NoDomainAccessFromExpositionRule");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.]+);");

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Regla para evitar acceso a domain desde exposition");
        descriptor.onlyOnLanguage("java");
    }

    @Override
    public void execute(SensorContext context) {
        FileSystem fs = context.fileSystem();

        for (InputFile inputFile : fs.inputFiles(fs.predicates().hasLanguage("java"))) {
            Path filePath = Path.of(inputFile.uri());
            if (!isInExpositionPackage(filePath)) {
                continue;
            }

            try {
                for (String line : inputFile.contents().lines().toList()) {
                    Matcher matcher = IMPORT_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String imported = matcher.group(1);
                        if (isDomainPackage(imported)) {
                            NewIssue issue = context.newIssue().forRule(RULE_KEY);
                            NewIssueLocation location = issue.newLocation()
                                .on(inputFile)
                                .message("No debes acceder directamente a clases del paquete domain desde exposition. Usa servicios o DTOs.");

                            issue.at(location).save();
                        }
                    }
                }
            } catch (IOException e) {
                // Manejo de error
            }
        }
    }

    private boolean isInExpositionPackage(Path path) {
        String normalizedPath = path.toString().replace("\\", "/").toLowerCase();
        return normalizedPath.contains("/exposition/") || normalizedPath.contains(".exposition.");
    }

    private boolean isDomainPackage(String imported) {
        return imported.contains(".domain.") 
            && (imported.contains(".model.") || imported.contains(".entity."));
    }
}
