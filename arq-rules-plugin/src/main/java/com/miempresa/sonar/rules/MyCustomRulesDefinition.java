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
            
        // CLAR003: Evitar lógica de negocio en la capa de exposition
        // Implementada en: NoBusinessLogicInExpositionRule.java
        repo.createRule("NoBusinessLogicInExpositionRule")
            .setName("Evita lógica de negocio en la capa de exposition")
            .setHtmlDescription("La capa de exposition no debe contener lógica de negocio. " + 
                "Delega la lógica de negocio a la capa de servicios.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "cleancode", "java");
        
        // CLAR004: Usar DTOs en la capa de exposition
        // Implementada en: UseDTOsInExpositionLayerRule.java
        repo.createRule("UseDTOsInExpositionLayerRule")
            .setName("Usa DTOs en la capa de exposition")
            .setHtmlDescription("La capa de exposition debe usar DTOs para la transferencia de datos. " + 
                "Evita exponer directamente las entidades del dominio.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "dto", "java");
        
            // CLAR005: Evitar modelo de dominio en la capa de exposition
        // Implementada en: AvoidDomainModelInExpositionRule.java
        repo.createRule("AvoidDomainModelInExpositionRule")
            .setName("Evita el modelo de dominio en la capa de exposition")
            .setHtmlDescription("No uses directamente el modelo de dominio en la capa de exposition. " + 
                "Utiliza DTOs para la transferencia de datos.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "domaindrivendesign", "java");
        
        // CLAR006: La capa de exposition debe comunicarse a través de interfaces de servicio
        // Implementada en: ExpositionCommunicatesViaServiceInterfacesRule.java
        repo.createRule("ExpositionCommunicatesViaServiceInterfacesRule")
            .setName("Exposition debe comunicarse mediante interfaces de servicio")
            .setHtmlDescription("La capa de exposition debe comunicarse con otras capas a través de interfaces de servicio. " + 
                "Evita dependencias directas con implementaciones concretas.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "interfaces", "java");
        
            // CLAR007: La capa de exposition debe delegar a servicios
        // Implementada en: ExpositionMustDelegateToServiceRule.java
        repo.createRule("ExpositionMustDelegateToServiceRule")
            .setName("Exposition debe delegar a servicios")
            .setHtmlDescription("La capa de exposition debe delegar las operaciones a servicios. " + 
                "No implementes la lógica directamente en los controladores.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "delegation", "java");
        
            // CLAR008: No retornar entidades de dominio desde exposition
        // Implementada en: NoDomainEntitiesReturnedFromExpositionRule.java
        repo.createRule("NoDomainEntitiesReturnedFromExpositionRule")
            .setName("No retornar entidades de dominio desde exposition")
            .setHtmlDescription("Los endpoints no deben retornar entidades de dominio directamente. " + 
                "Utiliza DTOs para encapsular la respuesta.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "domaindrivendesign", "java");
        
        // CLAR009: No dependencias de infraestructura desde exposition
        // Implementada en: NoInfrastructureDependencyFromExpositionRule.java
        repo.createRule("NoInfrastructureDependencyFromExpositionRule")
            .setName("No dependencias de infraestructura desde exposition")
            .setHtmlDescription("La capa de exposition no debe tener dependencias directas con la capa de infraestructura. " + 
                "Utiliza interfaces y servicios para el acceso a recursos externos.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "infrastructure", "java");
        
            // CLAR010: No acceso directo al modelo de repositorio desde infraestructura
        // Implementada en: NoDirectAccessToRepoModelInfraRule.java
        repo.createRule("NoDirectAccessToRepoModelInfraRule")
            .setName("No acceso directo al modelo de repositorio desde infraestructura")
            .setHtmlDescription("La capa de infraestructura no debe acceder directamente al modelo de repositorio. " + 
                "Utiliza interfaces y abstracciones apropiadas.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "exposition", "repositorypattern", "java");

        // Presentation
        // CLAR011
        // Implementada en: NoSqlOrJpaInControllerRule.java
        repo.createRule("NoSqlOrJpaInControllerRule")
            .setName("No acceso directo a la base de datos desde la capa de presentación")
            .setHtmlDescription("La capa de presentación no debe acceder directamente a la base de datos. " + "No SQL o JPA en los controladores. " +
                "Utiliza servicios y DTOs para la comunicación.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "presentation", "database", "java");
        
        // ServiceShouldNotDependOnControllerRule
        // CLAR012: La capa de servicio no debe depender de la capa de presentación
        repo.createRule("ServiceShouldNotDependOnControllerRule")
            .setName("No acceso directo a la capa de presentación desde la capa de servicio")
            .setHtmlDescription("La capa de servicio no debe acceder directamente a la capa de presentación. " + 
                "Utiliza interfaces y servicios para la comunicación.")
            .setSeverity("MAJOR")
            .setType(RuleType.CODE_SMELL)
            .setTags("architecture", "service", "java");
        repo.done();
    }
}