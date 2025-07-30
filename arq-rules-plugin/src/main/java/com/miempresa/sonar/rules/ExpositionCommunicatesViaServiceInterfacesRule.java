package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;

import java.nio.file.Path;

@Rule(key = "ExpositionCommunicatesViaServiceInterfacesRule")
public class ExpositionCommunicatesViaServiceInterfacesRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
        if (!isInExpositionPackage(context.getInputFile().path())) return;

        Symbol symbol = tree.identifier().symbol();

        if (symbol != null && symbol.type().isClass()) {
            String fqName = symbol.type().fullyQualifiedName().toLowerCase();
            if (isDomainOrInfraClass(fqName)) {
                context.reportIssue(this, tree, "La capa de exposición debe comunicarse solo con la capa de servicios mediante interfaces o DTOs.");
            }
        }

        super.visitMemberSelectExpression(tree);
    }

    @Override
    public void visitNewClass(NewClassTree tree) {
        if (!isInExpositionPackage(context.getInputFile().path())) return;

        if (tree.symbolType() != null) {
            String fqName = tree.symbolType().fullyQualifiedName().toLowerCase();
            if (isDomainOrInfraClass(fqName)) {
                context.reportIssue(this, tree, "La capa de exposición debe comunicarse solo con la capa de servicios mediante interfaces o DTOs.");
            }
        }

        super.visitNewClass(tree);
    }

    private boolean isInExpositionPackage(Path path) {
        String normalized = path.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }

    private boolean isDomainOrInfraClass(String fqName) {
        return fqName.contains(".domain.") || fqName.contains(".infrastructure.") || fqName.contains(".persistence.");
    }
}
