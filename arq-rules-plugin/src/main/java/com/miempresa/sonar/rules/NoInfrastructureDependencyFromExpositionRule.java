package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ImportTree;

import java.net.URI;

// ¿Qué hace?
// Cuando analiza un archivo en /exposition/, revisa cada import.
// Si el import apunta a un paquete considerado infraestructura, lanza un error.
// Evita que la capa de exposición tenga dependencias directas hacia adaptadores técnicos.

@Rule(key = "NoInfrastructureDependencyFromExpositionRule")
public class NoInfrastructureDependencyFromExpositionRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitImport(ImportTree tree) {
        String imported = tree.qualifiedIdentifier().toString();

        URI uri = context.getInputFile().uri();

        if (isInExpositionPackage(uri) && isInfrastructurePackage(imported)) {
            context.reportIssue(this, tree, "No uses dependencias de infraestructura o adaptadores técnicos desde la capa de exposición.");
        }

        super.visitImport(tree);
    }

    private boolean isInExpositionPackage(URI uri) {
        String normalized = uri.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }

    private boolean isInfrastructurePackage(String imported) {
        // Agrega aquí los paquetes que consideres infraestructura o adaptadores técnicos
        return imported.contains(".infrastructure.")
                || imported.contains(".adapter.")
                || imported.contains(".config.")
                || imported.contains(".persistence.")
                || imported.contains(".dao.")
                || imported.startsWith("javax.persistence")
                || imported.startsWith("jakarta.persistence")
                || imported.startsWith("org.hibernate")
                || imported.contains(".external.");
    }
}


// ¿Qué hace?
// Cuando analiza un archivo en /exposition/, revisa cada import.
// Si el import apunta a un paquete considerado infraestructura, lanza un error.
// Evita que la capa de exposición tenga dependencias directas hacia adaptadores técnicos.