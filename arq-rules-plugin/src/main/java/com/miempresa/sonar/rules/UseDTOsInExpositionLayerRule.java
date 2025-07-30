package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.nio.file.Path;


//  ¿Qué detecta esta regla?
// Caso	                                                       ¿Se reporta?
// public User getUser() (User está en .domain.model)	         Sí
// public UserDTO getUser() (UserDTO está fuera del dominio)	 No
// public ResponseEntity<User>	                                 Sí
// public void createUser(User user)	                         Sí


@Rule(key = "UseDTOsInExpositionLayerRule")
public class UseDTOsInExpositionLayerRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        Path filePath = context.getInputFile().path();
        if (!isInExpositionPackage(filePath)) {
            return;
        }

        // Revisar tipo de retorno
        if (tree.returnType() != null) {
            String returnType = tree.returnType().toString();
            if (isDomainType(returnType)) {
                context.reportIssue(this, tree.returnType(),
                    "No devuelvas entidades del dominio desde la capa de exposición. Usa DTOs.");
            }
        }

        // Revisar parámetros de entrada
        for (VariableTree param : tree.parameters()) {
            String paramType = param.type().toString();
            if (isDomainType(paramType)) {
                context.reportIssue(this, param,
                    "No uses entidades del dominio como parámetros en la capa de exposición. Usa DTOs.");
            }
        }

        super.visitMethod(tree);
    }

    private boolean isInExpositionPackage(Path path) {
        String normalized = path.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }

    private boolean isDomainType(String type) {
        return type.contains("domain.model") || type.contains("domain.entity");
    }
}
