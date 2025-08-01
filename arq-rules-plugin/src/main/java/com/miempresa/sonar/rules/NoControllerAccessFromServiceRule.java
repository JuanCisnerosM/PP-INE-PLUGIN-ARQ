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
    key = "NoControllerAccessFromServiceRule",
    name = "Services should not call controller or exposition classes",
    description = "Services should not call classes from controller or exposition packages. Communication must be unidirectional, from presentation to services.",
    priority = Priority.MAJOR,
    tags = {"arquitectura", "separacion-capas", "comunicacion-unidireccional"}
)
public class NoControllerAccessFromServiceRule implements Sensor {
    
    private static final List<Pattern> FORBIDDEN_IMPORTS = new ArrayList<>();
    static {
        // Importaciones de paquetes de presentación/exposición
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.presentacion\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.exposicion\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.controller\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.web\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.rest\\."));
        
        // Importaciones específicas de anotaciones de controladores
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.web\\.bind\\.annotation\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.stereotype\\.Controller"));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.web\\.bind\\.annotation\\.RestController"));
    }

    private static final List<Pattern> FORBIDDEN_USAGE_PATTERNS = new ArrayList<>();
    static {
        // Patrones de uso de clases de presentación/exposición
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Controller\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*RestController\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Endpoint\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Resource\\w*\\s+\\w+"));
        
        // Patrones de anotaciones de web/controladores en servicios
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@RequestMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@GetMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PostMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PutMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@DeleteMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PatchMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@Controller\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@RestController\\b"));
    }

    private static final Pattern SERVICE_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(servicios|services|aplicacion|application)\\b");
    private static final Pattern SERVICE_CLASS_PATTERN = Pattern.compile("@Service\\b|class\\s+\\w*Service\\w*");

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Controller Access From Service Check")
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
            
            // Solo analizar archivos que están en el paquete de servicios
            // o que son clases de servicio
            if (!SERVICE_PACKAGE_PATTERN.matcher(content).find() && 
                !SERVICE_CLASS_PATTERN.matcher(content).find()) {
                return;
            }

            // Buscar importaciones prohibidas
            for (Pattern pattern : FORBIDDEN_IMPORTS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoControllerAccessFromService"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Services no deben llamar directamente a clases del paquete controller ni exposición. La comunicación debe ser unidireccional, desde presentación hacia servicios."))
                        .save();
                    return; // Solo reportar una vez por archivo
                }
            }

            // Buscar patrones de uso prohibidos
            for (Pattern pattern : FORBIDDEN_USAGE_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoControllerAccessFromService"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Services no deben usar anotaciones o clases de la capa de presentación. La comunicación debe ser unidireccional, desde presentación hacia servicios."))
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

// La regla detecta:

// Importaciones prohibidas:

// Paquetes .presentacion.*, .exposicion.*, .controller.*
// Anotaciones de Spring Web (@RequestMapping, @Controller, etc.)
// Patrones de uso prohibidos:

// Variables o referencias a clases con nombres que contengan "Controller", "RestController", "Endpoint", "Resource"
// Uso de anotaciones de mapeo web en servicios

// Ejemplo que viola la regla

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

// import org.springframework.stereotype.Service;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.UsuarioController; // Violación

// @Service
// public class UsuarioService {
    
//     private UsuarioController controller; // Violación: Service llamando a Controller
    
//     public void procesarUsuario() {
//         controller.obtenerUsuarios(); // Violación: Comunicación hacia arriba
//     }
// }

// Ejemplo de código correcto:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

// import org.springframework.stereotype.Service;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia.UsuarioRepository;

// @Service
// public class UsuarioService {
    
//     private final UsuarioRepository repository; //  Correcto: Service usando Repository
    
//     public UsuarioService(UsuarioRepository repository) {
//         this.repository = repository;
//     }
    
//     public Usuario procesarUsuario(Long id) {
//         return repository.findById(id); //  Correcto: Comunicación hacia abajo
//     }
// }

// La regla generará un issue con el mensaje: "Los Services no deben llamar directamente a clases del paquete controller ni exposición. La comunicación debe ser unidireccional, desde presentación hacia servicios."

// Esta regla asegura que se mantenga una comunicación unidireccional en la arquitectura, donde los controladores llaman a los servicios, pero los servicios nunca llaman a los controladores.