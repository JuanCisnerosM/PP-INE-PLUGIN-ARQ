# Proyecto de Pruebas de Reglas Arquitectónicas

Este proyecto contiene ejemplos que demuestran tanto violaciones como cumplimientos de reglas arquitectónicas implementadas en el plugin de SonarQube.

## Estructura del Proyecto

```
src/
├── main/
│   └── java/
│       └── com/
│           └── miempresa/
│               └── pruebasespecificasdecapa/
│                   ├── domain/           # Entidades de dominio
│                   ├── exposition/       # Capa de exposición (API)
│                   │   ├── cli/         
│                   │   ├── dto/         # Objetos de transferencia de datos
│                   │   ├── rest/        # Controladores REST
│                   │   └── web/         
│                   ├── repository/       # Capa de persistencia
│                   │   └── entity/      # Entidades JPA
│                   └── service/         # Capa de servicios
│                       └── impl/        # Implementaciones de servicios
```

## Ejemplos de Violaciones de Reglas

### 1. UsuarioControllerMalo
Viola múltiples reglas:
- `NoRepositoryAccessFromExpositionRule`
- `NoDomainEntitiesReturnedFromExpositionRule`
- `NoBusinessLogicInExpositionRule`
- `AvoidDomainModelInExpositionRule`

### 2. UsuarioJpaControllerMalo
Viola:
- `NoSqlOrJpaInControllerRule`
- `NoDirectAccessToRepoModelInfraRule`

### 3. UsuarioInfraControllerMalo
Viola:
- `NoInfrastructureDependencyFromExpositionRule`
- `NoDirectAccessToRepoModelInfraRule`

### 4. UsuarioControllerSinInterfaz
Viola:
- `ExpositionCommunicatesViaServiceInterfacesRule`

### 5. UsuarioServiceMalo
Viola:
- `ServiceShouldNotDependOnControllerRule`

## Ejemplo de Buena Implementación

### UsuarioControllerBueno
Implementación que cumple con todas las reglas arquitectónicas:
- Usa DTOs para la transferencia de datos
- Se comunica a través de interfaces de servicio
- No contiene lógica de negocio
- No accede directamente al repositorio
- Mantiene una separación clara entre capas

## Lista Completa de Reglas

1. `NoRepositoryAccessFromExpositionRule`: La capa de exposición no debe acceder directamente a repositorios
2. `NoDomainEntitiesReturnedFromExpositionRule`: No retornar entidades de dominio desde la capa de exposición
3. `NoBusinessLogicInExpositionRule`: No implementar lógica de negocio en controladores
4. `AvoidDomainModelInExpositionRule`: No usar modelos de dominio en la capa de exposición
5. `ExpositionMustDelegateToServiceRule`: Los controladores deben delegar a servicios
6. `ExpositionCommunicatesViaServiceInterfacesRule`: Usar interfaces de servicio, no implementaciones
7. `UseDTOsInExpositionLayerRule`: Usar DTOs en la capa de exposición
8. `NoSqlOrJpaInControllerRule`: No usar JPA/SQL directamente en controladores
9. `NoInfrastructureDependencyFromExpositionRule`: No depender de infraestructura en la capa de exposición
10. `NoDirectAccessToRepoModelInfraRule`: No acceder directamente a modelos de repositorio
11. `ServiceShouldNotDependOnControllerRule`: Los servicios no deben depender de controladores
12. `NoDomainAccessFromExpositionRule`: No acceder al dominio desde la exposición

## Cómo Ejecutar

Para compilar el proyecto:
```bash
mvn clean install
```

## Requisitos

- Java 11 o superior
- Maven 3.6 o superior
- Las dependencias se gestionan a través de Maven (ver pom.xml)