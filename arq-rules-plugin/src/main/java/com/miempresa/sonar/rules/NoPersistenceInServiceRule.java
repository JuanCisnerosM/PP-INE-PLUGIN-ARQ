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
    key = "NoPersistenceInServiceRule",
    name = "Services should not contain direct persistence logic",
    description = "Services should not contain SQL, EntityManager, or direct data access logic. Persistence logic belongs to Repositories to maintain single responsibility principle.",
    priority = Priority.MAJOR,
    tags = {"arquitectura", "separacion-responsabilidades", "persistencia"}
)
public class NoPersistenceInServiceRule implements Sensor {
    
    private static final List<Pattern> PERSISTENCE_IMPORTS = new ArrayList<>();
    static {
        // Importaciones de persistencia JPA/Hibernate
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+javax\\.persistence\\.EntityManager"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+jakarta\\.persistence\\.EntityManager"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+javax\\.persistence\\.Query"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+jakarta\\.persistence\\.Query"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+javax\\.persistence\\.TypedQuery"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+jakarta\\.persistence\\.TypedQuery"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+org\\.hibernate\\.Session"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+org\\.hibernate\\.Query"));
        
        // Importaciones JDBC
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+java\\.sql\\.Connection"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+java\\.sql\\.PreparedStatement"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+java\\.sql\\.Statement"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+java\\.sql\\.ResultSet"));
        
        // Importaciones de Spring Data específicas de bajo nivel
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.jdbc\\.core\\.JdbcTemplate"));
        PERSISTENCE_IMPORTS.add(Pattern.compile("import\\s+org\\.springframework\\.jdbc\\.core\\.namedparam\\.NamedParameterJdbcTemplate"));
    }

    private static final List<Pattern> PERSISTENCE_ANNOTATIONS = new ArrayList<>();
    static {
        // Anotaciones de persistencia que no deberían estar en servicios
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@PersistenceContext\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@PersistenceUnit\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@Entity\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@Table\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@Column\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@Id\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@GeneratedValue\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@JoinColumn\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@OneToMany\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@ManyToOne\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@OneToOne\\b"));
        PERSISTENCE_ANNOTATIONS.add(Pattern.compile("@ManyToMany\\b"));
    }

    private static final List<Pattern> PERSISTENCE_USAGE_PATTERNS = new ArrayList<>();
    static {
        // Patrones de uso directo de persistencia
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("EntityManager\\s+\\w+"));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.createQuery\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.createNativeQuery\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.persist\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.merge\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.remove\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.find\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.flush\\("));
        
        // Patrones SQL
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("(?i)\\b(SELECT|INSERT|UPDATE|DELETE)\\s+.*\\bFROM\\b"));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("PreparedStatement\\s+\\w+"));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("Connection\\s+\\w+"));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.executeQuery\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.executeUpdate\\("));
        
        // Patrones de JdbcTemplate
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("JdbcTemplate\\s+\\w+"));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.query\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.update\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.queryForObject\\("));
        PERSISTENCE_USAGE_PATTERNS.add(Pattern.compile("\\.queryForList\\("));
    }

    private static final Pattern SERVICE_PACKAGE_PATTERN = Pattern.compile("package\\s+[\\w.]+\\.(servicios|services|aplicacion|application)\\b");
    private static final Pattern SERVICE_CLASS_PATTERN = Pattern.compile("@Service\\b|class\\s+\\w*Service\\w*");

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor
            .name("No Persistence Logic in Service Check")
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

            // Buscar importaciones de persistencia
            for (Pattern pattern : PERSISTENCE_IMPORTS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoPersistenceInService"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Services no deben contener SQL, EntityManager, ni lógica de acceso a datos directa. La lógica de persistencia pertenece a los Repositories."))
                        .save();
                    return; // Solo reportar una vez por archivo
                }
            }

            // Buscar anotaciones de persistencia
            for (Pattern pattern : PERSISTENCE_ANNOTATIONS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoPersistenceInService"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Services no deben usar anotaciones de persistencia. La lógica de persistencia pertenece a los Repositories."))
                        .save();
                    return; // Solo reportar una vez por archivo
                }
            }

            // Buscar patrones de uso de persistencia
            for (Pattern pattern : PERSISTENCE_USAGE_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    NewIssue issue = context.newIssue();
                    issue
                        .forRule(RuleKey.of("arq-rules-plugin", "NoPersistenceInService"))
                        .at(issue.newLocation()
                            .on(inputFile)
                            .message("Los Services no deben realizar operaciones de persistencia directamente. Use métodos del Repository en su lugar."))
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


// Esta regla detectará cuando un Service contenga lógica de persistencia directa.

// La regla detecta:

// Importaciones prohibidas:

// EntityManager, Query, TypedQuery (JPA/Hibernate)
// Connection, PreparedStatement, Statement, ResultSet (JDBC)
// JdbcTemplate (Spring JDBC)
// Anotaciones prohibidas:

// @PersistenceContext, @PersistenceUnit
// Anotaciones JPA: @Entity, @Table, @Column, @Id, etc.
// Anotaciones de relaciones: @OneToMany, @ManyToOne, etc.
// Patrones de uso prohibidos:

// Uso de EntityManager y sus métodos (persist, merge, find, etc.)
// Consultas SQL directas (SELECT, INSERT, UPDATE, DELETE)
// Uso de PreparedStatement, Connection
// Uso de JdbcTemplate y sus métodos

// Ejemplo de código que violaría esta regla:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

// import org.springframework.stereotype.Service;
// import javax.persistence.EntityManager; // ❌ Violación: Importación de persistencia
// import javax.persistence.PersistenceContext; // ❌ Violación

// @Service
// public class UsuarioService {
    
//     @PersistenceContext // ❌ Violación: Anotación de persistencia
//     private EntityManager entityManager;
    
//     public void guardarUsuario(Usuario usuario) {
//         entityManager.persist(usuario); // ❌ Violación: Lógica de persistencia directa
//     }
    
//     public List<Usuario> buscarPorNombre(String nombre) {
//         String sql = "SELECT u FROM Usuario u WHERE u.nombre = :nombre"; // ❌ Violación: SQL en servicio
//         return entityManager.createQuery(sql, Usuario.class)
//                 .setParameter("nombre", nombre)
//                 .getResultList();
//     }
// }

// Ejemplo de código correcto:

// package mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.servicios;

// import org.springframework.stereotype.Service;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.persistencia.UsuarioRepository;
// import mx.ipn.institucional.aplicaciones.proyectopruebasexcelcior.modelo.Usuario;

// @Service
// public class UsuarioService {
    
//     private final UsuarioRepository usuarioRepository; // ✅ Correcto: Usa Repository
    
//     public UsuarioService(UsuarioRepository usuarioRepository) {
//         this.usuarioRepository = usuarioRepository;
//     }
    
//     public void guardarUsuario(Usuario usuario) {
//         // Lógica de negocio
//         if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
//             throw new IllegalArgumentException("El nombre no puede estar vacío");
//         }
        
//         usuarioRepository.save(usuario); // ✅ Correcto: Delega al Repository
//     }
    
//     public List<Usuario> buscarPorNombre(String nombre) {
//         // Lógica de negocio
//         if (nombre == null) {
//             return Collections.emptyList();
//         }
        
//         return usuarioRepository.findByNombre(nombre); // ✅ Correcto: Usa Repository
//     }
// }

// Mensajes de error que generará:

// "Los Services no deben contener SQL, EntityManager, ni lógica de acceso a datos directa. La lógica de persistencia pertenece a los Repositories."
// "Los Services no deben usar anotaciones de persistencia. La lógica de persistencia pertenece a los Repositories."
// "Los Services no deben realizar operaciones de persistencia directamente. Use métodos del Repository en su lugar."
// Esta regla asegura que:

// Los servicios mantengan el principio de responsabilidad única
// La lógica de persistencia esté centralizada en los Repositories
// Los servicios se enfoquen en lógica de negocio
// El código sea más testeable y mantenible