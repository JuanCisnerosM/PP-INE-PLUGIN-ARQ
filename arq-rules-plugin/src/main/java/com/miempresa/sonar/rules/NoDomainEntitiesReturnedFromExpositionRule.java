package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.semantic.Type;

import java.net.URI;

@Rule(key = "NoDomainEntitiesReturnedFromExpositionRule")
public class NoDomainEntitiesReturnedFromExpositionRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        if (context == null || tree.returnType() == null) {
            super.visitMethod(tree);
            return;
        }

        URI uri = context.getInputFile().uri();
        if (isInExpositionPackage(uri)) {
            Type returnType = tree.symbol().returnType().type();

            if (returnType != null && isDomainPackage(returnType.fullyQualifiedName())) {
                context.reportIssue(this, tree.returnType(), "No devuelvas entidades del dominio desde la capa de exposición. Usa DTOs.");
            }
        }

        super.visitMethod(tree);
    }

    private boolean isInExpositionPackage(URI uri) {
        String normalized = uri.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }

    private boolean isDomainPackage(String fqn) {
        return fqn != null && fqn.contains(".domain.");
    }
}


