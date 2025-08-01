package com.miempresa.sonar.rules;

import org.sonar.api.Plugin;

public class MyCustomRulesPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // Registrar las clases que extienden la API de extensión
        // Clases base del plugin
        context.addExtension(MyCustomRulesDefinition.class);
        // CLAR001: Evitar acceso al dominio desde exposición
        context.addExtension(NoDomainAccessFromExpositionRule.class);
        // CLAR002: Evitar acceso a repositorios desde exposición
        context.addExtension(NoRepositoryAccessFromExpositionRule.class);
        // // CLAR003: Evitar lógica de negocio en exposición
        // context.addExtension(NoBusinessLogicInExpositionRule.class);
        // // CLAR004: Usar DTOs en la capa de exposición
        // context.addExtension(UseDTOsInExpositionLayerRule.class);
        // // CLAR005: Evitar modelo de dominio en exposición
        // context.addExtension(AvoidDomainModelInExpositionRule.class);
        // // CLAR006: Comunicación mediante interfaces de servicio
        // context.addExtension(ExpositionCommunicatesViaServiceInterfacesRule.class);
        // // CLAR007: Delegación a servicios
        // context.addExtension(ExpositionMustDelegateToServiceRule.class);
        // // CLAR008: No retornar entidades de dominio
        // context.addExtension(NoDomainEntitiesReturnedFromExpositionRule.class);
        // // CLAR009: No dependencias de infraestructura
        // context.addExtension(NoInfrastructureDependencyFromExpositionRule.class);
        // // CLAR010: No acceso directo al modelo de repositorio
        // context.addExtension(NoDirectAccessToRepoModelInfraRule.class);
        // // CLAR011: No acceso directo a la base de datos desde la capa de servicio
        // context.addExtension(NoSqlOrJpaInControllerRule.class);
        // // CLAR012: No acceso directo a la capa de presentación desde la capa de servicio
        // context.addExtension(ServiceShouldNotDependOnControllerRule.class);
    }
}
