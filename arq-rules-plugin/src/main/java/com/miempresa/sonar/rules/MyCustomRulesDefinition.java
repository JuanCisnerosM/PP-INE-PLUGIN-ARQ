package com.miempresa.sonar.rules;

import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

public class MyCustomRulesDefinition implements RulesDefinition {

    @Override
    public void define(@SuppressWarnings("null") Context context) {
        NewRepository repo = context.createRepository("arq-rules-plugin", "java");
        repo.setName("My Custom Layered Architecture Rules");

        // Define your custom rules here
        // Definimos la regla para evitar el acceso directo al paquete domain desde exposition
        repo.createRule("CLAR001")
            .setName("Evita acceso al paquete domain desde exposition")
            .setHtmlDescription("No debes acceder directamente a clases del paquete domain desde exposition. " + "Usa servicios o DTOs para manejar la lógica de negocio.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "domain-driven-design", "java");
        repo.done();

        // Definimos la regla para evitar el acceso directo al paquete repository desde exposition
        repo.createRule("CLAR002")
            .setName("Evita acceso al paquete repository desde exposition")
            .setHtmlDescription("No debes acceder directamente a clases del paquete repository desde exposition. " + "Usa servicios o DTOs para manejar la persistencia de datos.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "domain-driven-design", "java");
        repo.done();
    }
}