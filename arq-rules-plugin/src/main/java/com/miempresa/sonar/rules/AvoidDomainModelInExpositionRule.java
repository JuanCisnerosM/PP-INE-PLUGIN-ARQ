package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.nio.file.Path;

@Rule(key = "AvoidDomainModelInExpositionRule")
public class AvoidDomainModelInExpositionRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        if (!isInExpositionPackage(context.getInputFile().path())) return;

        // Verificar tipo de retorno
        String returnType = tree.returnType().symbolType().fullyQualifiedName();
        if (isDomainModel(returnType)) {
            context.reportIssue(this, tree.returnType(),
                    "No uses modelos internos como tipo de retorno. Usa DTOs en la entrada y salida.");
        }

        // Verificar parámetros de entrada
        for (VariableTree param : tree.parameters()) {
            String paramType = param.type().symbolType().fullyQualifiedName();
            if (isDomainModel(paramType)) {
                context.reportIssue(this, param,
                        "No uses modelos internos como parámetro de entrada. Usa DTOs.");
            }
        }

        super.visitMethod(tree);
    }

    private boolean isInExpositionPackage(Path path) {
        String normalized = path.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }

    private boolean isDomainModel(String fullyQualifiedName) {
        return fullyQualifiedName.contains(".domain.")
            || fullyQualifiedName.contains(".model.")
            || fullyQualifiedName.contains(".entity.");
    }
}
