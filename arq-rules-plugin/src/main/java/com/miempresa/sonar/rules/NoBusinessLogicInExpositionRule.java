package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;

import java.nio.file.Path;

@Rule(key = "NoBusinessLogicInExposition")
public class NoBusinessLogicInExpositionRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        CompilationUnitTree cut = context.getTree();
        if (cut.packageDeclaration() != null
                && cut.packageDeclaration().packageName().toString().contains(".exposition.")) {
            checkForBusinessLogic(tree.block());
        }
        super.visitMethod(tree);
    }

    private void checkForBusinessLogic(Tree tree) {
        if (tree == null) return;

        // Detectar estructuras de control que sugieren lógica compleja
        if (tree instanceof IfStatementTree
                || tree instanceof ForStatementTree
                || tree instanceof ForEachStatement
                || tree instanceof WhileStatementTree
                || tree instanceof SwitchStatementTree
                || tree instanceof DoWhileStatementTree
                || tree instanceof ConditionalExpressionTree) {
            context.reportIssue(this, tree, "No implementes lógica de control en los endpoints de exposición. Mueve esta lógica a la capa de servicio.");
        }

        // Detectar operaciones binarias que podrían ser lógica de negocio
        if (tree instanceof BinaryExpressionTree) {
            BinaryExpressionTree binary = (BinaryExpressionTree) tree;
            Tree.Kind kind = binary.kind();
            if (kind == Tree.Kind.CONDITIONAL_AND || kind == Tree.Kind.CONDITIONAL_OR
                    || kind == Tree.Kind.PLUS || kind == Tree.Kind.MINUS
                    || kind == Tree.Kind.MULTIPLY || kind == Tree.Kind.DIVIDE
                    || kind == Tree.Kind.EQUAL_TO || kind == Tree.Kind.NOT_EQUAL_TO) {
                context.reportIssue(this, tree, "No implementes lógica de negocio (operaciones complejas) en la capa de exposición.");
            }
        }

        // Detectar llamadas directas a lógica de dominio
        if (tree instanceof ExpressionStatementTree) {
            ExpressionTree expr = ((ExpressionStatementTree) tree).expression();
            if (expr instanceof MethodInvocationTree) {
                MethodInvocationTree methodCall = (MethodInvocationTree) expr;
                Symbol symbol = methodCall.symbol();
                if (symbol != null && symbol.owner().type().fullyQualifiedName().contains(".domain.")) {
                    context.reportIssue(this, tree, "Llamada directa a lógica de dominio en exposición. Usa servicios intermedios.");
                }
            }
        }

        // Recorrer hijos según tipo de nodo
        if (tree instanceof BlockTree) {
            for (StatementTree stmt : ((BlockTree) tree).body()) {
                checkForBusinessLogic(stmt);
            }
        } else if (tree instanceof IfStatementTree) {
            IfStatementTree ifTree = (IfStatementTree) tree;
            checkForBusinessLogic(ifTree.condition());
            checkForBusinessLogic(ifTree.thenStatement());
            if (ifTree.elseStatement() != null) {
                checkForBusinessLogic(ifTree.elseStatement());
            }
        } else if (tree instanceof MethodTree) {
            MethodTree methodTree = (MethodTree) tree;
            checkForBusinessLogic(methodTree.block());
        }
        // agregar más casos para otros nodos si se necesitan
    }
}

