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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoDomainAccessFromExpositionRule implements Sensor {

    private static final RuleKey RULE_KEY = RuleKey.of("arq-rules-plugin", "NoDomainAccessFromExpositionRule");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.]+);");
    
    // Patrones comunes para identificar paquetes de exposición
    private static final List<String> EXPOSITION_PATTERNS = Arrays.asList(
        "/exposicion/",
        ".exposicion.",
        "/exposition/",
        ".exposition.",
        "/rest/",
        ".rest.",
        "/controller/",
        ".controller.",
        "/controllers/",
        ".controllers.",
        "/api/",
        ".api."
    );

    // Patrones comunes para identificar paquetes de dominio/modelo
    private static final List<String> DOMAIN_PATTERNS = Arrays.asList(
        "/modelo/",
        ".modelo.",
        "/model/",
        ".model.",
        "/domain/",
        ".domain.",
        "/dominio/",
        ".dominio.",
        "/entity/",
        ".entity.",
        "/entities/",
        ".entities."
    );

    @Override
    public void describe(@javax.annotation.Nonnull SensorDescriptor descriptor) {
        descriptor.name("Regla para evitar acceso directo al modelo desde la capa de exposición");
        descriptor.onlyOnLanguage("java");
    }

    @Override
    public void execute(@javax.annotation.Nonnull SensorContext context) {
        FileSystem fs = context.fileSystem();

        for (InputFile inputFile : fs.inputFiles(fs.predicates().hasLanguage("java"))) {
            Path filePath = Path.of(inputFile.uri());
            
            // Solo analizar archivos en la capa de exposición
            if (!isInExpositionPackage(filePath)) {
                continue;
            }

            try {
                analyzeFile(inputFile, context);
            } catch (IOException e) {
                context.newAnalysisError()
                    .message("Error al analizar el archivo: " + e.getMessage())
                    .onFile(inputFile)
                    .save();
            }
        }
    }

    private void analyzeFile(InputFile inputFile, SensorContext context) throws IOException {
        String fileContent = inputFile.contents();
        String[] lines = fileContent.split("\n");
        
        // Analizar imports
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find()) {
                String imported = matcher.group(1);
                if (isDomainPackage(imported)) {
                    createIssue(context, inputFile, imported, i + 1, line.trim());
                }
            }
        }

        // Analizar el contenido del archivo en busca de referencias directas
        analyzeDirectReferences(context, inputFile, lines);
    }

    private boolean isInExpositionPackage(Path path) {
        String normalizedPath = path.toString().replace("\\", "/").toLowerCase();
        return EXPOSITION_PATTERNS.stream().anyMatch(pattern -> normalizedPath.contains(pattern));
    }

    private boolean isDomainPackage(String imported) {
        String normalizedImport = imported.toLowerCase();
        return DOMAIN_PATTERNS.stream().anyMatch(pattern -> normalizedImport.contains(pattern));
    }

    private boolean containsDirectDomainReferences(String content) {
        // Buscar patrones que indiquen uso directo de clases del dominio
        // Por ejemplo: new Entity(), @Autowired Entity, etc.
        String normalizedContent = content.toLowerCase();
        return DOMAIN_PATTERNS.stream()
            .anyMatch(pattern -> normalizedContent.contains(pattern.replace("/", "").replace(".", "")));
    }

    private void createIssue(SensorContext context, InputFile inputFile, String importedClass, int line, String codeLine) {
        NewIssue issue = context.newIssue().forRule(RULE_KEY);
        NewIssueLocation location = issue.newLocation()
            .on(inputFile)
            .at(inputFile.selectLine(line))
            .message(String.format(
                "No se debe acceder directamente a clases del modelo/dominio '%s' desde la capa de exposición.\n" +
                "Línea problemática: %s\n" +
                "Solución: Utiliza DTOs y servicios para acceder a los datos del modelo.",
                importedClass,
                codeLine
            ));

        issue.at(location).save();
    }

    private void analyzeDirectReferences(SensorContext context, InputFile inputFile, String[] lines) {

        Pattern fieldPattern = Pattern.compile("(private|protected|public)?\\s*([\\w\\<\\>]+)\\s+(\\w+)\\s*;");
        Pattern methodPattern = Pattern.compile("(private|protected|public)?\\s*([\\w\\<\\>]+)\\s+(\\w+)\\s*\\(");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Buscar declaraciones de campos
            Matcher fieldMatcher = fieldPattern.matcher(line);
            if (fieldMatcher.find()) {
                String type = fieldMatcher.group(2);
                if (isDomainType(type)) {
                    createIssue(context, inputFile, type, i + 1, line.trim());
                }
            }

            // Buscar declaraciones de métodos
            Matcher methodMatcher = methodPattern.matcher(line);
            if (methodMatcher.find()) {
                String returnType = methodMatcher.group(2);
                if (isDomainType(returnType)) {
                    createIssue(context, inputFile, returnType, i + 1, line.trim());
                }
            }
        }
    }

    private boolean isDomainType(String typeParam) {
        // Eliminar genéricos si existen
        final String type = typeParam.contains("<") 
            ? typeParam.substring(0, typeParam.indexOf("<"))
            : typeParam;
        
        // Comprobar si el tipo pertenece a un paquete de dominio
        return DOMAIN_PATTERNS.stream()
            .anyMatch(pattern -> type.toLowerCase().contains(pattern.replace("/", "").replace(".", "")));
    }
}

