package com.miempresa.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ImportTree;


// Detecta si alguna clase en el paquete service hace referencia a clases en los paquetes controller,exposition,rest,cli,etc.
// Si una clase ubicada en un paquete que contiene"service"importa o hace referencia a clases que están en paquetes como"controller","exposition","rest","cli",debe marcarse como violación.

// Esta regla:
// Funciona en fase de compilación.
// Detecta cualquier clase de la capa service que importe directamente una clase de controller o exposition.
// Asegura que la comunicación siga siendo unidireccional: controller → service, y no al revés.

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
