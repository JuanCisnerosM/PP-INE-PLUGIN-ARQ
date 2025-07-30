package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.nio.file.Path;
import java.util.List;

// Criterios de detección:
// El archivo está en un paquete exposition.
// Se busca dentro de métodos (MethodTree).
// Si un método contiene más de una llamada a método, o múltiples expresiones (asignaciones, condiciones, ciclos, etc.), se reporta.


@Rule(key = "ExpositionMustDelegateToServiceRule")
public class ExpositionMustDelegateToServiceRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        if (!isInExpositionPackage(context.getInputFile().path())) return;

        BlockTree body = tree.block();
        if (body == null) return;

        List<StatementTree> statements = body.body();
        int meaningfulStatements = 0;

        for (StatementTree statement : statements) {
            if (statement.is(Tree.Kind.EXPRESSION_STATEMENT)
                    || statement.is(Tree.Kind.IF_STATEMENT)
                    || statement.is(Tree.Kind.FOR_STATEMENT)
                    || statement.is(Tree.Kind.WHILE_STATEMENT)
                    || statement.is(Tree.Kind.TRY_STATEMENT)
                    || statement.is(Tree.Kind.RETURN_STATEMENT)
                    || statement.is(Tree.Kind.VARIABLE)) {
                meaningfulStatements++;
            }
        }

        // Si hay más de una instrucción relevante, probablemente no se delega directamente
        if (meaningfulStatements > 1) {
            context.reportIssue(this, tree.simpleName(),
                    "El controlador debe delegar todas las operaciones al Service o capa de aplicación, no implementar lógica propia.");
        }

        super.visitMethod(tree);
    }

    private boolean isInExpositionPackage(Path path) {
        String normalized = path.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }
}
