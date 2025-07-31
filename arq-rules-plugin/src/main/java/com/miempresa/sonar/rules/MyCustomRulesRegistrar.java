package com.miempresa.sonar.rules;

import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;


public class MyCustomRulesRegistrar implements CheckRegistrar {
    @Override @NonNull
    public void register(RegistrarContext context) {
        // Registramos las reglas personalizadas definidas en MyCustomRulesDefinition
        // TAG: Aquí se registran las clases de chequeo que implementan las reglas personalizadas
        // Estas clases deben extender JavaCheck y estar anotadas con @Rule 
        @SuppressWarnings("unchecked") // Cast necesario para evitar advertencias de tipo unchecked
        List<Class<? extends JavaCheck>> checks = (List<Class<? extends JavaCheck>>)(List<?>) Arrays.asList(
            // CLAR001
            NoDomainAccessFromExpositionRule.class,
            // CLAR002
            NoRepositoryAccessFromExpositionRule.class,
            // CLAR003
            NoBusinessLogicInExpositionRule.class,
            // CLAR004
            UseDTOsInExpositionLayerRule.class,
            // CLAR005
            AvoidDomainModelInExpositionRule.class,
            // CLAR006
            ExpositionCommunicatesViaServiceInterfacesRule.class,
            // CLAR007
            ExpositionMustDelegateToServiceRule.class,
            // CLAR008
            NoDomainEntitiesReturnedFromExpositionRule.class,
            // CLAR009
            NoInfrastructureDependencyFromExpositionRule.class,
            // CLAR010
            NoDirectAccessToRepoModelInfraRule.class,
            // CLAR011
            NoSqlOrJpaInControllerRule.class,
            // CLAR012
            ServiceShouldNotDependOnControllerRule.class
        );
        context.registerClassesForRepository(
            "arq-rules-plugin", // repositorio declarado en RulesDefinition
            checks,
            Collections.emptyList()
        );
    }
}
