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
    key = "NoFrameworkDependenciesInDomain",
    name = "Domain/Model should not have framework dependencies",
    description = "Domain/Model layer should not have Spring dependencies like @Component, @Service, @Repository. It must be completely framework independent.",
    priority = Priority.MAJOR,
    tags = {"arquitectura", "domain-driven-design", "independencia-framework"}
)
public class NoFrameworkDependenciesInDomainRule implements Sensor {
    
    private static final List<Pattern> FRAMEWORK_ANNOTATIONS = new ArrayList<>();
    static {
        // Anotaciones de Spring Framework
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Component\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Service\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Repository\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Controller\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@RestController\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Configuration\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Bean\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Autowired\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Inject\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Value\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Qualifier\\b"));
        
        // Anotaciones de persistencia/transacciones
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@Transactional\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@PersistenceContext\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@PersistenceUnit\\b"));
        
        // Anotaciones de validación que pueden indicar dependencia del framework
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@RequestMapping\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@GetMapping\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@PostMapping\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@PutMapping\\b"));
        FRAMEWORK_ANNOTATIONS.add(Pattern.compile("@DeleteMapping\\b"));
    }

    private static final List<Pattern> FRAMEWORK_IMPORTS = new ArrayList<>();
    static {
        // Importaciones de Spring
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\."));
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+javax\\.inject\\."));
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+jakarta\\.inject\\."));
        
        // Importaciones de persistencia
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+javax\\.persistence\\."));
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+jakarta\\.persistence\\."));
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+org\\.hibernate\\."));
        
        // Importaciones de validación que pueden indicar dependencia del framework
        FRAMEWORK_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.web\\."));
    }

    private static final Pattern DOMAIN_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(modelo|domain)\\b");

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Framework Dependencies in Domain Check")
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
            
            // Solo analizar archivos que están en el paquete de dominio/modelo
            if (!DOMAIN_PACKAGE_PATTERN.matcher(content).find()) {
                return;
            }

            // Buscar anotaciones prohibidas del framework
            for (Pattern pattern : FRAMEWORK_ANNOTATIONS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoFrameworkDependenciesInDomain"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("El dominio/modelo no debe tener dependencias con Spring (@Component, @Service, @Repository). Debe ser completamente independiente del framework."))
                        .save();
                    return; // Solo reportar una vez por archivo
                }
            }

            // Buscar importaciones prohibidas del framework
            for (Pattern pattern : FRAMEWORK_IMPORTS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoFrameworkDependenciesInDomain"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("El dominio/modelo no debe importar clases de Spring u otros frameworks. Debe ser completamente independiente del framework."))
                        .save();
                    return; // Solo reportar una vez por archivo
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

// Esta regla detectará cuando clases en el paquete modelo o domain tengan dependencias de framework, específicamente:

// Anotaciones prohibidas:

// @Component, @Service, @Repository
// @Controller, @RestController
// @Autowired, @Inject, @Value
// @Transactional
// @PersistenceContext
// Anotaciones de mapeo web (@RequestMapping, @GetMapping, etc.)
// Importaciones prohibidas:

// org.springframework.*
// javax.inject.* / jakarta.inject.*
// javax.persistence.* / jakarta.persistence.*
// org.hibernate.*

// Ejemplo de código que violaría esta regla
// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo;

// import org.springframework.stereotype.Component;
// import org.springframework.beans.factory.annotation.Autowired;

// @Component // Violación: El dominio no debe tener anotaciones de Spring
// public class Usuario {
    
//     @Autowired // Violación: El dominio no debe usar inyección de dependencias
//     private SomeService service;
    
//     private String nombre;
//     private String email;
    
//     // getters y setters
// }

// Ejemplo de código correcto para el dominio:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo;

// // Sin importaciones de framework
// public class Usuario {
    
//     private String nombre;
//     private String email;
    
//     public Usuario(String nombre, String email) {
//         this.nombre = nombre;
//         this.email = email;
//     }
    
//     // getters y setters puros
//     // lógica de negocio pura (sin dependencias externas)
// }

// La regla generará un issue con el mensaje: "El dominio/modelo no debe tener dependencias con Spring (@Component, @Service, @Repository). Debe ser completamente independiente del framework."

// Esta regla asegura que el dominio mantenga su independencia del framework, siguiendo los principios de Domain-Driven Design y arquitectura hexagonal