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
            NoDomainAccessFromExpositionRule.class,
            NoRepositoryAccessFromExpositionRule.class
            // AvoidUselessComments.class
        );
        context.registerClassesForRepository(
            "arq-rules-plugin", // repositorio declarado en RulesDefinition
            checks,
            Collections.emptyList()
        );
    }
}
