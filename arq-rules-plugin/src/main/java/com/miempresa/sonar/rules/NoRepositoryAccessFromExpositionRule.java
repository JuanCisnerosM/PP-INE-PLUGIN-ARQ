package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Rule(key = "NoRepositoryAccessFromExpositionRule")
public class NoRepositoryAccessFromExpositionRule implements Sensor {

    private static final RuleKey RULE_KEY = RuleKey.of("arq-rules-plugin", "NoRepositoryAccessFromExpositionRule");

    // Patrones para detectar paquetes de exposición
    private static final List<String> EXPOSITION_PATTERNS = Arrays.asList(
        "/exposition/", ".exposition.",
        "/rest/", ".rest.",
        "/controller/", ".controller.",
        "/controllers/", ".controllers.",
        "/api/", ".api.",
        "/resource/", ".resource.",
        "/resources/", ".resources."
    );

    // Patrones para detectar paquetes de repositorio
    private static final List<String> REPOSITORY_PATTERNS = Arrays.asList(
        "/repository/", ".repository.",
        "/repositories/", ".repositories.",
        "/persistence/", ".persistence.",
        "/dao/", ".dao.",
        "/daos/", ".daos.",
        "/infraestructure/", ".infraestructure.",
        "/infra/", ".infra."
    );

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.]+);");
    private static final Pattern CLASS_FIELD_PATTERN = Pattern.compile("(?:private|protected|public)\\s+([\\w\\<\\>\\[\\]]+)\\s+\\w+\\s*;");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?:private|protected|public)\\s+([\\w\\<\\>\\[\\]]+)\\s+\\w+\\s*\\([^\\)]*\\)");
    private static final Pattern METHOD_PARAM_PATTERN = Pattern.compile("([\\w\\<\\>\\[\\]]+)\\s+\\w+");

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("No Repository Access From Exposition Rule")
            .onlyOnLanguage("java");
    }

    @Override
    public void execute(SensorContext context) {
        for (InputFile inputFile : context.fileSystem().inputFiles(context.fileSystem().predicates().hasLanguage("java"))) {
            if (isInExpositionPackage(inputFile)) {
                try {
                    analyzeFile(inputFile, context);
                } catch (IOException e) {
                    // Log error or handle appropriately
                }
            }
        }
    }

    private void analyzeFile(InputFile inputFile, SensorContext context) throws IOException {
        String content = new String(Files.readAllBytes(inputFile.file().toPath()), StandardCharsets.UTF_8);
        
        // Verificar imports
        Matcher importMatcher = IMPORT_PATTERN.matcher(content);
        while (importMatcher.find()) {
            String imported = importMatcher.group(1);
            if (isRepositoryPattern(imported)) {
                createIssue(context, inputFile, importMatcher.start(), String.format(
                    "No debes importar clases del repositorio ('%s') directamente en la capa de exposición.\n" +
                    "Solución: Utiliza servicios para acceder a los datos. Los controladores solo deben comunicarse con servicios.",
                    imported
                ));
            }
        }

        // Verificar campos de clase
        Matcher fieldMatcher = CLASS_FIELD_PATTERN.matcher(content);
        while (fieldMatcher.find()) {
            String type = fieldMatcher.group(1);
            if (isRepositoryPattern(type)) {
                createIssue(context, inputFile, fieldMatcher.start(), String.format(
                    "No debes declarar variables de tipo repositorio ('%s') en la capa de exposición.\n" +
                    "Solución: Inyecta servicios en lugar de repositorios. Los controladores deben depender de servicios.",
                    type
                ));
            }
        }

        // Verificar métodos (tipo de retorno y parámetros)
        Matcher methodMatcher = METHOD_PATTERN.matcher(content);
        while (methodMatcher.find()) {
            String returnType = methodMatcher.group(1);
            if (isRepositoryPattern(returnType)) {
                createIssue(context, inputFile, methodMatcher.start(), String.format(
                    "El método no debe retornar tipos del repositorio ('%s') en la capa de exposición.\n" +
                    "Solución: Retorna DTOs o tipos de dominio a través de servicios.",
                    returnType
                ));
            }

            // Verificar parámetros del método
            String params = content.substring(methodMatcher.end());
            int closingParen = params.indexOf(')');
            if (closingParen >= 0) {
                String parameters = params.substring(0, closingParen);
                Matcher paramMatcher = METHOD_PARAM_PATTERN.matcher(parameters);
                while (paramMatcher.find()) {
                    String paramType = paramMatcher.group(1);
                    if (isRepositoryPattern(paramType)) {
                        createIssue(context, inputFile, methodMatcher.start() + paramMatcher.start(), String.format(
                            "El parámetro no debe ser de tipo repositorio ('%s') en la capa de exposición.\n" +
                            "Solución: Usa DTOs o tipos de dominio como parámetros y delega la lógica de persistencia a los servicios.",
                            paramType
                        ));
                    }
                }
            }
        }
    }

    private boolean isInExpositionPackage(InputFile file) {
        String path = file.toString().replace("\\", "/").toLowerCase();
        return EXPOSITION_PATTERNS.stream().anyMatch(path::contains);
    }

    private boolean isRepositoryPattern(String type) {
        if (type == null) return false;
        String normalizedType = type.toLowerCase();
        return REPOSITORY_PATTERNS.stream().anyMatch(normalizedType::contains);
    }

    private void createIssue(SensorContext context, InputFile file, int position, String message) {
        try {
            // Convertir posición de carácter a número de línea
            String content = file.contents();
            int lineNumber = 1;
            for (int i = 0; i < position && i < content.length(); i++) {
                if (content.charAt(i) == '\n') {
                    lineNumber++;
                }
            }
            
            NewIssue issue = context.newIssue().forRule(RULE_KEY);
            issue.at(issue.newLocation()
                .on(file)
                .at(file.selectLine(lineNumber))
                .message(message));
            issue.save();
        } catch (IOException e) {
            // Log error or handle appropriately
        }
    }
}
