Primero checamos la version de SonarQube Docker

```bash
(Invoke-WebRequest -Uri http://localhost:9000/api/server/version).Content
```
Salida:
```
25.6.0.109173
```

Segundo, checamos la version del plugin de java

```bash
docker exec -it sonarqube ls /opt/sonarqube/lib/extensions/ | findstr java
```
Salida:
```
sonar-java-plugin-8.14.1.39293.jar
```

Tercero, creamos el arquetipo Maven con:

```bash
mvn archetype:generate -DgroupId=com.miempresa.sonar -DartifactId=arq-rules-plugin -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Cuarto, instalar manualmente los archivos en la raiz del repositorio local maven, mueve los 4 rchivos .jar y .pom a la raiz del proyecto, y ejecuta los siguientes comandos:

```bash


Es la API principal de SonarQube. Contiene las interfaces y clases base que se utilizan para desarrollar plugins. Es la dependencia que defines en tu pom.xml para el desarrollo del plugin

```bash
mvn install:install-file \
-Dfile=sonar-plugin-api-9.14.0.375.jar \
-DpomFile=sonar-plugin-api-9.14.0.375.pom \
-DgroupId=org.sonarsource.sonarqube \
-DartifactId=sonar-plugin-api \
-Dversion=9.14.0.375 \
-Dpackaging=jar
```
Es la implementación de la API. Contiene las clases concretas que implementan las interfaces definidas en la API. Se usa internamente por SonarQube

```bash
mvn install:install-file \
-Dfile=sonar-plugin-api-impl-9.9.0.65466.jar \
-DpomFile=sonar-plugin-api-impl-9.9.0.65466.pom \
-DgroupId=org.sonarsource.sonarqube \
-DartifactId=sonar-plugin-api \
-Dversion=9.9.0.65466 \
-Dpackaging=jar
```

Descargar el sonar-java-plugin-8.14.1.39293.jar y sonar-java-plugin-8.14.1.39293.pom desde el repositorio de SonarQube y ejecuta:

```bash
docker cp sonar-java-plugin-8.14.1.39293.jar sonarqube:/opt/sonarqube/extensions/plugins/
```

Creas una carpeta dentro de sonar llamada "rules"
Y creas los siguientes archivos:
- MyCustomRulesDefinition.java : Este archivo es donde definirás las reglas personalizadas que SonarQube usará. Aquí defines el comportamiento de las reglas, como su nombre, descripción, severidad, tipo, y parámetros si es necesario.
- MyCustomRulesPlugin.java : Este archivo es el plugin propiamente dicho, donde registrarás las definiciones y las reglas creadas. El archivo MyCustomRulesDefinition.java se registra aquí.
- MyCustomRulesRegistrar.java : Este archivo es para registrar las reglas en el sistema. Normalmente, no es estrictamente necesario para un plugin básico, pero si deseas registrar dinámicamente más reglas o usar funcionalidades avanzadas (como reglas que dependen de parámetros dinámicos), entonces se usa este archivo.

Creamos las reglas, empaquetamos el Maven y copiamos el .JAR a sonarQube y reiniciamos

```bash
docker cp target/arq-rules-plugin-1.0-SNAPSHOT.jar sonarqube:/opt/sonarqube/extensions/plugins/
```
y despues

```bash
docker restart sonarqube
```



Para las PruebasEspecificas

Este proyecto está organizado siguiendo una arquitectura en capas que permite separar responsabilidades, mejorar el mantenimiento del código y facilitar la validación con herramientas como SonarQube.

## 🧱 Estructura del Proyecto por Capas
### 📌 Descripción de cada capa
- **exposition/**: Contiene los controladores o puntos de entrada, como REST, CLI o interfaces web. No debe contener lógica de negocio.
- **service/**: Contiene la lógica de aplicación que orquesta los flujos entre la exposición y el dominio.
- **domain/**: Incluye las entidades del negocio, lógica pura y validaciones. No depende de otras capas.
- **repository/**: Se encarga del acceso a datos y define las interfaces para interactuar con fuentes de datos.

### 📂 Estructura de carpetas
```
src/main/java/com/miempresa/miapp/
├── exposition/             ← Capa de Exposición
│   ├── rest/               ← (Capa de ) Presentación vía REST
│   ├── cli/                ← (Opcional) Presentación vía línea de comandos
│   └── web/                ← (Opcional) Presentación web/MVC tradicional
│
├── service/                ← Capa de Servicios (lógica de aplicación)
│
├── domain/                 ← Capa de Dominio (modelos, entidades, lógica de negocio pura)
│
├── repository/             ← Capa de Persistencia (interfaces y adaptadores de datos)
```


Reglas:

Exsposicion:
- NoDomainAccessFromExpositionRule: 
  - Un endpoint de exposición (por ejemplo, REST o CLI) no debe acceder 
directamente a clases del paquete domain 
    - Solo debe comunicarse con servicios mediante interfaces o 
DTOs (Data Transfer Object). 

- NoRepositoryAccessFromExpositionRule
  - Un endpoint de exposición no debe acceder directamente a clases del 
paquete repository 
    - Toda persistencia debe manejarse dentro de los servicios 



Presentacion:
- NoPersistenceInControllerRule:
  - Un Controller no debe contener consultas SQL o llamadas a 
EntityManager, JPA, JDBC 
   - La persistencia es responsabilidad de los Repository
- NoOtherLayerAnnotationsInControllerRule:
  - Un Controller no debe tener anotaciones de otras capas, como 
@Service o @Repository 
   - Su rol debe limitarse estrictamente a manejar peticiones.




Servicio/Aplicacion:


Dominio/Modelo:
- NoFrameworkDependenciesInDomainRule
  - No debe tener dependencias con Spring (@Component, @Service, 
@Repository) 
   - Debe ser completamente independiente del framework.
Persistencia:

