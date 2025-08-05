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
    key = "NoUpperLayerAccessFromRepository",
    name = "Repositories should not call service or presentation layer classes",
    description = "Repositories should not call classes from service or presentation layers. This breaks layered architecture and creates cycles between infrastructure ↔ service ↔ presentation.",
    priority = Priority.CRITICAL,
    tags = {"arquitectura", "capas", "dependencias-ciclicas", "infraestructura"}
)
public class NoUpperLayerAccessFromRepositoryRule implements Sensor {
    
    private static final List<Pattern> FORBIDDEN_IMPORTS = new ArrayList<>();
    static {
        // Importaciones de capa de servicios/aplicación
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.servicios\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.services\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.service\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.aplicacion\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.application\\."));
        
        // Importaciones de capa de presentación/exposición
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.presentacion\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.presentation\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.exposicion\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.exposition\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.controller\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.controllers\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.web\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.rest\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.api\\."));
        
        // Importaciones de DTOs (que generalmente están en capas superiores)
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.dto\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.dtos\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.request\\."));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+[\\w.]+\\.response\\."));
        
        // Importaciones específicas de anotaciones de capas superiores
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.stereotype\\.Service"));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.stereotype\\.Controller"));
        FORBIDDEN_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.web\\.bind\\.annotation\\."));
    }

    private static final List<Pattern> FORBIDDEN_USAGE_PATTERNS = new ArrayList<>();
    static {
        // Patrones de uso de clases de capas superiores
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Service\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Controller\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*RestController\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Endpoint\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Resource\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*DTO\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Request\\w*\\s+\\w+"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("\\b\\w*Response\\w*\\s+\\w+"));
        
        // Anotaciones de capas superiores que no deberían estar en repositorios
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@Service\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@Controller\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@RestController\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@RequestMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@GetMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PostMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PutMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@DeleteMapping\\b"));
        FORBIDDEN_USAGE_PATTERNS.add(Pattern.compile("@PatchMapping\\b"));
    }

    private static final Pattern REPOSITORY_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(persistencia|persistence|repository|repositories|infrastructure|infraestructura|dao|daos)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPOSITORY_CLASS_PATTERN = Pattern.compile("@Repository\\b|class\\s+\\w*Repository\\w*\\s*\\{|interface\\s+\\w*Repository\\w*\\s*\\{", Pattern.CASE_INSENSITIVE);

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Upper Layer Access From Repository Check")
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
            
            // Verificar si está en paquete de repositorio por package statement o por ruta
            boolean isRepositoryPackage = REPOSITORY_PACKAGE_PATTERN.matcher(content).find() || 
                                        REPOSITORY_CLASS_PATTERN.matcher(content).find() ||
                                        isRepositoryPath(inputFile.toString());
            
            if (!isRepositoryPackage) {
                return;
            }

            // Buscar importaciones prohibidas
            for (Pattern pattern : FORBIDDEN_IMPORTS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoUpperLayerAccessFromRepository"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Repositories no deben llamar directamente a clases de la capa de servicios o presentación. Esto rompe la arquitectura en capas y genera ciclos de dependencias."))
                        .save();
                    return; // Solo reportar una vez por archivo
                }
            }

            // Buscar patrones de uso prohibidos
            for (Pattern pattern : FORBIDDEN_USAGE_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoUpperLayerAccessFromRepository"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Repositories no deben usar clases o anotaciones de capas superiores (servicios/presentación). Esto rompe la arquitectura en capas."))
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

    private boolean isRepositoryPath(String filePath) {
        String normalizedPath = filePath.replace("\\", "/").toLowerCase();
        return normalizedPath.contains("/persistencia/") || 
               normalizedPath.contains("/persistence/") || 
               normalizedPath.contains("/repository/") || 
               normalizedPath.contains("/repositories/") || 
               normalizedPath.contains("/infrastructure/") || 
               normalizedPath.contains("/infraestructura/") || 
               normalizedPath.contains("/dao/") || 
               normalizedPath.contains("/daos/");
    }
}


// Esta regla detectará cuando un Repository tenga dependencias o llame a clases de capas superiores.

// La regla detecta:

// Importaciones prohibidas de capas superiores:

// Paquetes de servicios: .servicios.*, .services.*, .aplicacion.*, .application.*
// Paquetes de presentación: .presentacion.*, .exposicion.*, .controller.*, .web.*, .rest.*, .api.*
// DTOs: .dto.*, .dtos.*, .request.*, .response.*
// Anotaciones de Spring Web y Service
// Patrones de uso prohibidos:

// Variables o referencias a clases con nombres que contengan "Service", "Controller", "DTO", "Request", "Response"
// Uso de
// anotaciones de

// capas superiores (@Service, @Controller, @RequestMapping, etc.)

// Ejemplo de código que violaría esta regla:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia;

// import org.springframework.stereotype.Repository;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios.UsuarioService; // ❌ CRÍTICO: Import de servicio
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.presentacion.UsuarioController; // ❌ CRÍTICO: Import de controller
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.dto.UsuarioDTO; // ❌ CRÍTICO: Import de DTO

// @Repository
// public class UsuarioRepository {
    
//     private UsuarioService usuarioService; // ❌ CRÍTICO: Repository llamando a Service
//     private UsuarioController controller; // ❌ CRÍTICO: Repository llamando a Controller
    
//     public void save(Usuario usuario) {
//         // ❌ CRÍTICO: Lógica que debería estar en el servicio
//         usuarioService.validarUsuario(usuario);
        
//         // Lógica de persistencia correcta
//         entityManager.persist(usuario);
//     }
// }

// Ejemplo de código correcto:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia;

// import org.springframework.stereotype.Repository;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario; // ✅ Correcto: Import de entidad de dominio
// import javax.persistence.EntityManager;
// import javax.persistence.PersistenceContext;

// @Repository
// public class UsuarioRepository {
    
//     @PersistenceContext
//     private EntityManager entityManager; // ✅ Correcto: Solo usa herramientas de persistencia
    
//     public void save(Usuario usuario) {
//         entityManager.persist(usuario); // ✅ Correcto: Solo lógica de persistencia
//     }
    
//     public Usuario findById(Long id) {
//         return entityManager.find(Usuario.class, id); // ✅ Correcto: Solo acceso a datos
//     }
    
//     public List<Usuario> findByNombre(String nombre) {
//         return entityManager.createQuery(
//             "SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class)
//             .setParameter("nombre", nombre)
//             .getResultList(); // ✅ Correcto: Solo consultas de datos
//     }
// }

// Mensajes de error que generará:

// "Los Repositories no deben llamar directamente a clases de la capa de servicios o presentación. Esto rompe la arquitectura en capas y genera ciclos de dependencias."
// "Los Repositories no deben usar clases o anotaciones de capas superiores (servicios/presentación). Esto rompe la arquitectura en capas."
// ¿Por qué es CRÍTICA esta regla?

// Rompe la arquitectura en capas: Crea dependencias hacia arriba
// Genera ciclos de dependencias: Infrastructure ↔ Service ↔ Presentation
// Hace el código imposible de testear: Los repositorios dependen de toda la aplicación
// Viola el principio de inversión de dependencias: Las capas de bajo nivel dependen de las de alto nivel
// Hace imposible el reuso: Los repositorios no pueden usarse independientemente
// Esta regla es fundamental para mantener una arquitectura en capas limpia y bien estructurada.