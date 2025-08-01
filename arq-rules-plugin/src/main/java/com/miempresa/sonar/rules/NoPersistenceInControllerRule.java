package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.check.Priority;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Rule(
    key = "NoPersistenceInControllerRule",
    name = "Controllers should not contain direct persistence operations",
    description = "Controllers should not contain SQL queries or direct calls to EntityManager, JPA, JDBC. Persistence operations should be handled by Repositories.",
    priority = Priority.MAJOR,
    tags = {"arquitectura", "buenas-practicas"}
)
public class NoPersistenceInControllerRule implements Sensor {
    
    private static final List<Pattern> PERSISTENCE_PATTERNS = new ArrayList<>();
    static {
        // Patrones de importación
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+javax\\.persistence\\.EntityManager"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+jakarta\\.persistence\\.EntityManager"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+java\\.sql\\.Connection"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+org\\.hibernate\\.Session"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+javax\\.persistence\\.Query"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+jakarta\\.persistence\\.Query"));
        
        // Patrones de uso
        PERSISTENCE_PATTERNS.add(Pattern.compile("@PersistenceContext"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("EntityManager\\s+\\w+"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("\\.createQuery\\("));
        PERSISTENCE_PATTERNS.add(Pattern.compile("\\.createNativeQuery\\("));
        PERSISTENCE_PATTERNS.add(Pattern.compile("\\.persist\\("));
        PERSISTENCE_PATTERNS.add(Pattern.compile("\\.merge\\("));
        PERSISTENCE_PATTERNS.add(Pattern.compile("PreparedStatement"));
        PERSISTENCE_PATTERNS.add(Pattern.compile("Connection\\s+\\w+"));
        
        // Patrones específicos para la estructura de paquetes institucional
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+[\\w.]+\\.persistencia\\."));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+javax\\.persistence\\."));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+org\\.springframework\\.data\\.jpa\\."));
        PERSISTENCE_PATTERNS.add(Pattern.compile("import\\s+org\\.hibernate\\."));
    }

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile("@(Rest)?Controller\\s+");
    private static final Pattern PRESENTATION_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(presentacion|exposicion)\\b");

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Persistence in Controller Check")
            .onlyOnLanguage("java");
    }

    @Override
    public void execute(@Nonnull SensorContext context) {
        FileSystem fs = context.fileSystem();
        
        Iterable<InputFile> inputFiles = fs.inputFiles(fs.predicates().hasLanguage("java"));
        
        for (InputFile inputFile : inputFiles) {
            analyzeFile(inputFile, context);
        }
    }

    private void analyzeFile(InputFile inputFile, SensorContext context) {
        try {
            String content = new String(inputFile.contents().getBytes(), StandardCharsets.UTF_8);
            
            // Solo analizar archivos que están en los paquetes de presentación o exposición
            // y que son controladores
            if (!PRESENTATION_PACKAGE_PATTERN.matcher(content).find() || 
                !CONTROLLER_PATTERN.matcher(content).find()) {
                return;
            }

            // Buscar violaciones de persistencia
            for (Pattern pattern : PERSISTENCE_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    // Crear un problema
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoPersistenceInController"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Controllers no deben realizar operaciones de persistencia directamente. Use un Repository para esto."))
                        .save();
                    break;
                }
            }
            
        } catch (IOException e) {
            // Manejar error de lectura de archivo
            context.newAnalysisError()
                .message("Error al analizar el archivo: " + e.getMessage())
                .onFile(inputFile)
                .save();
        }
    }
}
