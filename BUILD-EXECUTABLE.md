# Generar Ejecutable de KodeForge

Este documento explica cómo generar un ejecutable nativo de KodeForge para diferentes plataformas.

---

## 📋 Requisitos Previos

### Para Windows (.exe)
- **JDK 17 o superior** instalado
- **Gradle** (incluido en el proyecto con wrapper)
- **jpackage** (incluido en JDK 17+)

### Para macOS (.app / .dmg)
- **JDK 17 o superior** instalado
- **Xcode Command Line Tools** (para firmar la app)

### Para Linux (.deb / .rpm)
- **JDK 17 o superior** instalado
- **fakeroot** y **dpkg** (para .deb)
- **rpmbuild** (para .rpm)

---

## 🚀 Métodos de Empaquetado

### Método 1: Usando Compose Desktop Plugin (Recomendado)

El proyecto ya está configurado con el plugin de Compose Desktop que facilita la creación de ejecutables.

#### 1.1 Generar ejecutable para la plataforma actual

```bash
# En Windows (genera .exe)
./gradlew.bat packageExe

# En macOS (genera .dmg)
./gradlew packageDmg

# En Linux (genera .deb)
./gradlew packageDeb
```

#### 1.2 Generar instalador completo

```bash
# Windows - Instalador MSI
./gradlew.bat packageMsi

# macOS - Paquete PKG
./gradlew packagePkg

# Linux - Paquete RPM
./gradlew packageRpm
```

#### 1.3 Ubicación de los ejecutables generados

Los archivos generados se encuentran en:
```
build/compose/binaries/main/
├── app/
│   └── KodeForge/          # Aplicación sin empaquetar
├── exe/
│   └── KodeForge.exe       # Ejecutable Windows
├── dmg/
│   └── KodeForge.dmg       # Imagen de disco macOS
├── deb/
│   └── kodeforge_1.0.0.deb # Paquete Debian
└── msi/
    └── KodeForge.msi       # Instalador Windows
```

---

### Método 2: Usando jpackage Manualmente

Si prefieres más control sobre el proceso de empaquetado:

#### 2.1 Compilar el proyecto

```bash
./gradlew clean build
```

#### 2.2 Crear JAR ejecutable

```bash
./gradlew jar
```

El JAR se genera en: `build/libs/kodeforge.jar`

#### 2.3 Usar jpackage

**Windows (.exe):**
```bash
jpackage \
  --input build/libs \
  --name KodeForge \
  --main-jar kodeforge.jar \
  --main-class com.kodeforge.ui.MainKt \
  --type exe \
  --icon src/jvmMain/resources/icon.ico \
  --app-version 1.0.0 \
  --vendor "KodeForge Team" \
  --description "Herramienta de gestión de proyectos y desarrollo" \
  --win-shortcut \
  --win-menu
```

**macOS (.dmg):**
```bash
jpackage \
  --input build/libs \
  --name KodeForge \
  --main-jar kodeforge.jar \
  --main-class com.kodeforge.ui.MainKt \
  --type dmg \
  --icon src/jvmMain/resources/icon.icns \
  --app-version 1.0.0 \
  --vendor "KodeForge Team" \
  --description "Herramienta de gestión de proyectos y desarrollo" \
  --mac-package-name com.kodeforge.app
```

**Linux (.deb):**
```bash
jpackage \
  --input build/libs \
  --name kodeforge \
  --main-jar kodeforge.jar \
  --main-class com.kodeforge.ui.MainKt \
  --type deb \
  --icon src/jvmMain/resources/icon.png \
  --app-version 1.0.0 \
  --vendor "KodeForge Team" \
  --description "Herramienta de gestión de proyectos y desarrollo" \
  --linux-shortcut
```

---

## ⚙️ Configuración Avanzada

### Personalizar el build.gradle.kts

Puedes personalizar la configuración del empaquetado editando `build.gradle.kts`:

```kotlin
compose.desktop {
    application {
        mainClass = "com.kodeforge.ui.MainKt"
        
        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,    // macOS
                TargetFormat.Msi,    // Windows instalador
                TargetFormat.Exe,    // Windows ejecutable
                TargetFormat.Deb,    // Linux Debian/Ubuntu
                TargetFormat.Rpm     // Linux RedHat/Fedora
            )
            
            packageName = "KodeForge"
            packageVersion = "1.0.0"
            description = "Herramienta de gestión de proyectos y desarrollo"
            copyright = "© 2026 KodeForge Team"
            vendor = "KodeForge"
            
            // Iconos por plataforma
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                menuGroup = "KodeForge"
                upgradeUuid = "UNIQUE-UUID-HERE"
            }
            
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
                bundleID = "com.kodeforge.app"
                // Firma de código (opcional)
                // signing {
                //     sign.set(true)
                //     identity.set("Developer ID Application: Your Name")
                // }
            }
            
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                packageName = "kodeforge"
                debMaintainer = "team@kodeforge.com"
                menuGroup = "Development"
            }
        }
    }
}
```

---

## 🎨 Crear Iconos para la Aplicación

### Requisitos de iconos:

- **Windows**: `.ico` (256x256, 128x128, 64x64, 48x48, 32x32, 16x16)
- **macOS**: `.icns` (1024x1024, 512x512, 256x256, 128x128, 64x64, 32x32, 16x16)
- **Linux**: `.png` (512x512 recomendado)

### Herramientas para crear iconos:

**Online:**
- [CloudConvert](https://cloudconvert.com/) - Convertir PNG a ICO/ICNS
- [iConvert Icons](https://iconverticons.com/) - Convertir entre formatos

**Desktop:**
- [GIMP](https://www.gimp.org/) - Editor de imágenes gratuito
- [Inkscape](https://inkscape.org/) - Editor vectorial gratuito

### Ubicación de los iconos:

```
src/jvmMain/resources/
├── icon.ico      # Windows
├── icon.icns     # macOS
└── icon.png      # Linux
```

---

## 🔧 Solución de Problemas

### Error: "jpackage not found"

**Solución:** Asegúrate de tener JDK 17+ instalado y que esté en el PATH:

```bash
# Verificar versión de Java
java -version

# Verificar que jpackage está disponible
jpackage --version
```

### Error: "Module not found"

**Solución:** Asegúrate de que todas las dependencias estén incluidas en el JAR:

```bash
# Crear un fat JAR con todas las dependencias
./gradlew shadowJar
```

### El ejecutable no inicia

**Solución:** Verifica los logs:

**Windows:**
```
%LOCALAPPDATA%\KodeForge\logs\app.log
```

**macOS:**
```
~/Library/Logs/KodeForge/app.log
```

**Linux:**
```
~/.local/share/KodeForge/logs/app.log
```

### Error de permisos en macOS

**Solución:** Firma la aplicación o permite aplicaciones de desarrolladores no identificados:

```bash
# Permitir la aplicación
xattr -cr /Applications/KodeForge.app

# O desde Preferencias del Sistema
# Seguridad y Privacidad > General > "Abrir de todas formas"
```

---

## 📦 Distribución

### Windows

1. **Ejecutable portable (.exe):**
   - Distribuir directamente el archivo `.exe`
   - No requiere instalación
   - Tamaño: ~100-150 MB

2. **Instalador (.msi):**
   - Instalación guiada
   - Crea accesos directos
   - Registro en el sistema
   - Tamaño: ~100-150 MB

### macOS

1. **Imagen de disco (.dmg):**
   - Arrastrar y soltar a Aplicaciones
   - Estándar en macOS
   - Tamaño: ~100-150 MB

2. **Paquete instalador (.pkg):**
   - Instalación guiada
   - Requiere permisos de administrador
   - Tamaño: ~100-150 MB

### Linux

1. **Paquete Debian (.deb):**
   ```bash
   sudo dpkg -i kodeforge_1.0.0.deb
   ```

2. **Paquete RPM (.rpm):**
   ```bash
   sudo rpm -i kodeforge-1.0.0.rpm
   ```

---

## 🚀 Script de Build Automatizado

Crea un script para automatizar el proceso:

**build-all.sh** (Linux/macOS):
```bash
#!/bin/bash

echo "🔨 Compilando KodeForge..."
./gradlew clean build

echo "📦 Generando ejecutables..."
./gradlew packageDmg packageDeb

echo "✅ Build completado!"
echo "📁 Archivos en: build/compose/binaries/main/"
```

**build-all.bat** (Windows):
```batch
@echo off
echo 🔨 Compilando KodeForge...
gradlew.bat clean build

echo 📦 Generando ejecutables...
gradlew.bat packageExe packageMsi

echo ✅ Build completado!
echo 📁 Archivos en: build\compose\binaries\main\
pause
```

---

## 📊 Tamaños Aproximados

| Plataforma | Formato | Tamaño Aprox. |
|------------|---------|---------------|
| Windows    | .exe    | 120 MB        |
| Windows    | .msi    | 125 MB        |
| macOS      | .dmg    | 130 MB        |
| macOS      | .pkg    | 135 MB        |
| Linux      | .deb    | 115 MB        |
| Linux      | .rpm    | 115 MB        |

**Nota:** Los tamaños incluyen el JRE embebido para que la aplicación funcione sin necesidad de tener Java instalado.

---

## 🎯 Checklist de Release

- [ ] Actualizar versión en `build.gradle.kts`
- [ ] Compilar y probar en modo release
- [ ] Generar ejecutables para todas las plataformas
- [ ] Probar instalación en sistemas limpios
- [ ] Verificar que los iconos se muestren correctamente
- [ ] Crear notas de la versión (CHANGELOG.md)
- [ ] Firmar ejecutables (opcional pero recomendado)
- [ ] Subir a repositorio de releases
- [ ] Actualizar documentación de usuario

---

## 📚 Referencias

- [Compose Multiplatform Packaging](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution)
- [jpackage Documentation](https://docs.oracle.com/en/java/javase/17/jpackage/)
- [Gradle Application Plugin](https://docs.gradle.org/current/userguide/application_plugin.html)

---

## 💡 Consejos

1. **Reduce el tamaño:** Usa ProGuard o R8 para minimizar el JAR
2. **Actualización automática:** Implementa un sistema de actualizaciones
3. **Logs:** Incluye logging para facilitar el debug en producción
4. **Crash reports:** Considera integrar un sistema de reporte de errores
5. **Firma de código:** Firma tus ejecutables para evitar advertencias de seguridad

---

**¿Necesitas ayuda?** Abre un issue en el repositorio del proyecto.

