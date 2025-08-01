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
    key = "NoOtherLayerAnnotationsInController",
    name = "Controllers should not have annotations from other layers",
    description = "Controllers should not have annotations like @Service or @Repository. Their role should be limited strictly to handling requests.",
    priority = Priority.MAJOR,
    tags = {"arquitectura", "buenas-practicas", "separacion-capas"}
)
public class NoOtherLayerAnnotationsInControllerRule implements Sensor {
    
    private static final List<Pattern> FORBIDDEN_ANNOTATIONS = new ArrayList<>();
    static {
        // Anotaciones de capa de servicio
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Service\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Component\\b"));
        
        // Anotaciones de capa de persistencia
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Repository\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Entity\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Table\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Embeddable\\b"));
        
        // Anotaciones de configuración
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Configuration\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Bean\\b"));
        
        // Anotaciones de transacciones (debería estar en servicios)
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@Transactional\\b"));
        
        // Anotaciones de persistencia
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@PersistenceContext\\b"));
        FORBIDDEN_ANNOTATIONS.add(Pattern.compile("@PersistenceUnit\\b"));
    }

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile("@(Rest)?Controller\\b");
    private static final Pattern PRESENTATION_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(presentacion|exposicion)\\b");

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Other Layer Annotations in Controller Check")
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

            // Buscar anotaciones prohibidas
            for (Pattern pattern : FORBIDDEN_ANNOTATIONS) {
                if (pattern.matcher(content).find()) {
                    // Crear un problema
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoOtherLayerAnnotationsInController"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Controllers no deben tener anotaciones de otras capas como @Service o @Repository. Su rol debe limitarse a manejar peticiones."))
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
