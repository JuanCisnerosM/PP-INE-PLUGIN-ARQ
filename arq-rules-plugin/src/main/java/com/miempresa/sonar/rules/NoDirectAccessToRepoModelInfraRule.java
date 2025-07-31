package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ImportTree;
import java.net.URI;

@Rule(key = "NoDirectAccessToRepoModelInfra")
public class NoDirectAccessToRepoModelInfraRule extends BaseTreeVisitor implements JavaFileScanner {

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
        String importPath = tree.qualifiedIdentifier().toString();

        if (importPath.contains(".repository.") || importPath.contains(".model.") || importPath.contains(".infrastructure.")) {
            context.reportIssue(this, tree, "No debes tener dependencias directas hacia Repository, Model o Infrastructure en la capa de exposición.");
        }

        super.visitImport(tree);
    }

    private boolean isInExpositionPackage(URI uri) {
        String normalized = uri.toString().replace("\\", "/").toLowerCase();
        return normalized.contains("/exposition/") || normalized.contains(".exposition.");
    }
}
