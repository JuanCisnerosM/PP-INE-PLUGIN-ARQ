package com.miempresa.sonar.rules;

import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

public class MyCustomRulesDefinition implements RulesDefinition {

    @Override
    public void define(@SuppressWarnings("null") Context context) {
        NewRepository repo = context.createRepository("arq-rules-plugin", "java");
        repo.setName("My Custom Layered Architecture Rules");

        // Exposition
        // CLAR001: Evitar acceso directo al paquete domain desde exposition
        // Implementada en: NoDomainAccessFromExpositionRule.java
        repo.createRule("NoDomainAccessFromExpositionRule")
            .setName("Evita acceso al paquete domain desde exposition")
            .setHtmlDescription("No debes acceder directamente a clases del paquete domain desde exposition. " + 
                "Usa servicios o DTOs para manejar la lógica de negocio.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "domaindrivendesign", "java");

        // CLAR002: Evitar acceso directo al paquete repository desde exposition
        // Implementada en: NoRepositoryAccessFromExpositionRule.java
        repo.createRule("NoRepositoryAccessFromExpositionRule")
            .setName("Evita acceso al paquete repository desde exposition")
            .setHtmlDescription("No debes acceder directamente a clases del paquete repository desde exposition. " + 
                "Usa servicios o DTOs para manejar la persistencia de datos.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "domaindrivendesign", "java");
            

        // Presentation
        // CLAR011: No persistencia en controladores
        // Implementada en: NoPersistenceInControllerRule.java
        repo.createRule("NoPersistenceInControllerRule")
            .setName("No acceso directo a la base de datos desde la capa de presentación")
            .setHtmlDescription("Los Controllers no deben contener consultas SQL o llamadas directas a EntityManager, JPA, JDBC. " +
                "La persistencia es responsabilidad de los Repositories.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "presentation", "database", "java");

        // CLAR012: No anotaciones de otras capas en controladores
        // Implementada en: NoOtherLayerAnnotationsInControllerRule.java
        repo.createRule("NoOtherLayerAnnotationsInControllerRule")
            .setName("Los Controllers no deben tener anotaciones de otras capas")
            .setHtmlDescription("Los Controllers no deben tener anotaciones como @Service o @Repository. " +
                "Su rol debe limitarse estrictamente a manejar peticiones.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "presentation", "annotations", "java");

            
        // Servicios/Aplicación
        // CLAR021: No acceso a controladores desde servicios
        // Implementada en: NoControllerAccessFromServiceRule.java
        repo.createRule("NoControllerAccessFromServiceRule")
            .setName("Los Services no deben llamar a clases del controller ni exposición")
            .setHtmlDescription("Los Services no deben llamar directamente a clases del paquete controller ni exposición. " +
                "La comunicación debe ser unidireccional, desde presentación hacia servicios.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "services", "unidirectional-communication", "java");

        // Domain/Modelo
        // CLAR031: No dependencias de framework en dominio
        // Implementada en: NoFrameworkDependenciesInDomainRule.java
        repo.createRule("NoFrameworkDependenciesInDomainRule")
            .setName("El dominio/modelo no debe tener dependencias de framework")
            .setHtmlDescription("El dominio/modelo no debe tener dependencias con Spring (@Component, @Service, @Repository). " +
                "Debe ser completamente independiente del framework.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "domain", "framework-independence", "java");
        
        repo.done();
    }
}