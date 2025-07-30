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

Cuarto, instalar manualmente los archivos en la raiz del repositorio local maven


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
