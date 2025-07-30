package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Objects;

@Rule(key = "ServiceShouldNotDependOnControllerRule")
public class ServiceShouldNotDependOnControllerRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;

        String filePath = context.getInputFile().uri().toString();
        if (filePath.contains("/service/") || filePath.contains(".service.")) {
            scan(context.getTree());
        }
    }

    @Override
    public void visitImport(ImportTree tree) {
        String imported = tree.qualifiedIdentifier().toString();
        if (isPresentationPackage(imported)) {
            context.reportIssue(this, tree, "La capa de servicios no debe depender de controladores o clases de exposición.");
        }
        super.visitImport(tree);
    }

    private boolean isPresentationPackage(String imported) {
        return imported.contains(".controller")
            || imported.contains(".exposition")
            || imported.contains(".rest")
            || imported.contains(".cli");
    }
}
