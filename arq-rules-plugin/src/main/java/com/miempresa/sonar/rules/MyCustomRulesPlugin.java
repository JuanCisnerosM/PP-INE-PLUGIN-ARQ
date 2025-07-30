package com.miempresa.sonar.rules;

import org.sonar.api.Plugin;

public class MyCustomRulesPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // Registrar las clases que extienden la API de extensión
        context.addExtension(MyCustomRulesDefinition.class);
        context.addExtension(MyCustomRulesRegistrar.class);
        context.addExtension(NoRepositoryAccessFromExpositionRule.class);
        context.addExtension(NoDomainAccessFromExpositionRule.class);
    }
}
