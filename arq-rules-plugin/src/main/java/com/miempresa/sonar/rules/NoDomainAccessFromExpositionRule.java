package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ImportTree;
import java.nio.file.Path;

@Rule(key = "NoDomainAccessFromExpositionRule")
public class NoDomainAccessFromExpositionRule extends BaseTreeVisitor implements JavaFileScanner{
    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitImport(ImportTree tree) {
        String imported = tree.qualifiedIdentifier().toString();

        Path filePath = Path.of(context.getInputFile().uri());
        if (isInExpositionPackage(filePath) && isDomainPackage(imported)) {
            context.reportIssue(this, tree, "No debes acceder directamente a clases del paquete domain desde exposition. Usa servicios o DTOs.");
        }

        super.visitImport(tree);
    }
    
    private boolean isInExpositionPackage(Path path) {
        String normalizedPath = path.toString().replace("\\", "/").toLowerCase();
        return normalizedPath.contains("/exposition/") || normalizedPath.contains(".exposition.");
    }

    private boolean isDomainPackage(String imported) {
        return imported.contains(".domain.")
        && (imported.contains(".model.") || imported.contains(".entity."));
    }
}
