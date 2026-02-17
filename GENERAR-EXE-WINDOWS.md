# Generar Ejecutable para Windows

## Requisitos Previos
- **JDK 14 o superior instalado** (se recomienda JDK 17 o superior)
  - **IMPORTANTE:** JDK 11 o anteriores NO incluyen `jpackage`, que es necesario para crear ejecutables
  - Verifica tu versión con: `java -version`
  - Descarga JDK 17+ desde: https://adoptium.net/ o https://www.oracle.com/java/technologies/downloads/
- WiX Toolset 3.11+ (para formato MSI) - Opcional pero recomendado
  - Descargar de: https://wixtoolset.org/releases/
  - Agregar al PATH de Windows

## Comandos para Generar Distribuciones

### 1. Generar Ejecutable EXE (más simple)
```powershell
.\gradlew.bat packageExe
```

**Nota:** Si el comando `.\gradlew.bat` no funciona, asegúrate de que el archivo `gradlew.bat` existe en el directorio raíz del proyecto.

Este comando generará un instalador EXE en:
```
build\compose\binaries\main\exe\
```

### 2. Generar Instalador MSI (recomendado)
```powershell
.\gradlew.bat packageMsi
```

Este comando generará un instalador MSI en:
```
build\compose\binaries\main\msi\
```

**Nota:** Para MSI necesitas WiX Toolset instalado.

### 3. Generar Todas las Distribuciones
```powershell
.\gradlew.bat packageDistributionForCurrentOS
```

Este comando genera todos los formatos disponibles para tu sistema operativo actual.

### 4. Generar Distribución Universal
```powershell
.\gradlew.bat package
```

Genera todas las distribuciones configuradas (Dmg, Msi, Exe).

## Formatos de Salida Configurados

El proyecto está configurado para generar:
- **Windows**: MSI (instalador), EXE (ejecutable)
- **macOS**: DMG (imagen de disco)

## Ubicación de los Archivos Generados

Después de ejecutar los comandos, encontrarás los archivos en:
```
build/compose/binaries/main/
  ├── exe/          # Instaladores EXE para Windows
  ├── msi/          # Instaladores MSI para Windows
  └── dmg/          # Imágenes DMG para macOS
```

## Ejecutar sin Generar Distribución

Si solo quieres ejecutar la aplicación sin crear un instalador:

```powershell
.\gradlew.bat run
```

## Solución de Problemas

### Error: "jpackage.exe is missing" o "Failed to check JDK distribution"
Este es el error más común. Significa que tu JDK no incluye la herramienta `jpackage`.

**Solución:**
1. Verifica tu versión de Java:
   ```powershell
   java -version
   ```

2. Si tienes JDK 11 o anterior, necesitas actualizar a JDK 14+:
   - Descarga JDK 17 o superior desde:
     - **Adoptium (Eclipse Temurin)**: https://adoptium.net/ (Recomendado)
     - **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/
   
3. Instala el nuevo JDK y configura la variable de entorno `JAVA_HOME`:
   ```powershell
   # Verificar JAVA_HOME actual
   echo $env:JAVA_HOME
   
   # Configurar JAVA_HOME (ajusta la ruta según tu instalación)
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-17', 'User')
   
   # Reinicia tu terminal después de cambiar JAVA_HOME
   ```

4. Verifica que `jpackage` esté disponible:
   ```powershell
   jpackage --version
   ```

### Error: "WiX Toolset not found"
- Instala WiX Toolset desde https://wixtoolset.org/releases/
- Agrega `C:\Program Files (x86)\WiX Toolset v3.11\bin` al PATH
- Reinicia tu terminal

### Error: "Java heap space"
Aumenta la memoria disponible en `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Error al compilar
Asegúrate de que todas las dependencias estén actualizadas:
```powershell
.\gradlew.bat clean build
```

## Personalización del Icono (Opcional)

Para agregar un icono personalizado a tu aplicación de Windows:

1. Crea o consigue un archivo `.ico` (formato Windows)
2. Colócalo en `src/jvmMain/resources/icon.ico`
3. Descomenta la línea en `build.gradle.kts`:
   ```kotlin
   iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
   ```

## Distribución del Ejecutable

Los archivos generados (MSI o EXE) son instaladores independientes que:
- Incluyen el JVM embebido (no requieren Java instalado)
- Instalan la aplicación en `Program Files`
- Crean accesos directos en el menú de inicio
- Permiten desinstalación desde el Panel de Control

El instalador tendrá un tamaño aproximado de 150-250 MB debido al JVM embebido.
