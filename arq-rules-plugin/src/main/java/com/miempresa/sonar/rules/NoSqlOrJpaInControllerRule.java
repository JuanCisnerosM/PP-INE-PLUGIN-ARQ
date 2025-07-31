package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.net.URI;

// Capa de Presentacion

// Esta regla detecta patrones comunes que indican acceso directo a la base de datos desde clases en el paquete controller o exposition, tales como:
// Clases de javax.persistence.EntityManager
// Uso de java.sql.Connection, Statement, ResultSet
// Cadenas que contengan SQL (SELECT, INSERT, UPDATE, DELETE)

@Rule(key = "NoSqlOrJpaInController")
public class NoSqlOrJpaInControllerRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        URI uri = context.getInputFile().uri();

        if (isInExpositionPackage(uri)) {
            scan(context.getTree());
        }
    }

    @Override
    public void visitImport(ImportTree tree) {
        String importStr = tree.qualifiedIdentifier().toString();

        if (importStr.contains("javax.persistence.EntityManager")
                || importStr.contains("java.sql.")
                || importStr.contains("jakarta.persistence.EntityManager")) {
            context.reportIssue(this, tree,
                    "No uses EntityManager ni JDBC en la capa de exposición.");
        }

        super.visitImport(tree);
    }

    @Override
    public void visitLiteral(LiteralTree tree) {
        String value = tree.value().toLowerCase();
        if (value.contains("select ") || value.contains("insert ")
                || value.contains("update ") || value.contains("delete ")) {
            context.reportIssue(this, tree,
                    "No escribas sentencias SQL directamente en la capa de exposición.");
        }

        super.visitLiteral(tree);
    }

    private boolean isInExpositionPackage(URI uri) {
        String normalized = uri.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/controller/") || normalized.contains("/exposition/") || normalized.contains(".controller.") || normalized.contains(".exposition.");
    }
}
