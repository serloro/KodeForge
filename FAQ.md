# Preguntas Frecuentes (FAQ)

## ❓ ¿Por qué tarda tanto en generar el EXE?

El proceso incluye:
1. **Compilar todo el código Kotlin** (~1-2 min)
2. **Resolver dependencias** (~1-2 min en primera ejecución)
3. **Crear imagen de aplicación** (~2-3 min)
4. **Empaquetar con JVM embebido** (~2-4 min)
5. **Generar el instalador** (~1-2 min)

**Total:** 5-10 minutos la primera vez, 2-5 minutos en siguientes compilaciones.

---

## ❓ ¿El EXE funciona sin Java instalado?

**Sí**, el ejecutable generado incluye:
- JVM completo embebido
- Todas las dependencias de la aplicación
- Todo el código compilado

El usuario final NO necesita tener Java instalado.

---

## ❓ ¿Por qué el EXE es tan grande (~200 MB)?

El tamaño incluye:
- **JVM embebido:** ~150 MB
- **Tu aplicación:** ~20-30 MB
- **Dependencias:** ~20-30 MB

Esto es normal para aplicaciones Java/Kotlin nativas.

---

## ❓ ¿Puedo reducir el tamaño del EXE?

Opciones limitadas:
1. **jlink:** Crear JVM mínimo (requiere configuración avanzada)
2. **ProGuard/R8:** Ofuscar y minimizar (complejo en Compose)
3. **Comprimir:** El instalador ya está comprimido

Para la mayoría de casos, ~200 MB es aceptable.

---

## ❓ ¿Diferencia entre EXE y MSI?

### EXE (Executable Installer)
- ✅ Más simple de usar
- ✅ No requiere herramientas adicionales
- ✅ Instalación con doble clic
- ❌ Menos opciones de personalización

### MSI (Windows Installer)
- ✅ Estándar de Microsoft
- ✅ Soporte para Group Policy
- ✅ Mejor para empresas
- ✅ Más opciones de instalación
- ❌ Requiere WiX Toolset para generar

**Recomendación:** Usa **EXE** para distribución general, **MSI** para entornos corporativos.

---

## ❓ ¿Necesito firmar el ejecutable?

**Para distribución pública:** Sí, es muy recomendado.

Sin firma:
- Windows SmartScreen mostrará advertencias
- Los usuarios tendrán que hacer clic en "Ejecutar de todas formas"

Para firmar necesitas:
1. Certificado de firma de código (Code Signing Certificate)
2. Herramienta signtool.exe (incluida en Windows SDK)

```powershell
signtool sign /f "tu-certificado.pfx" /p "password" /t http://timestamp.digicert.com "KodeForge-1.0.0.exe"
```

---

## ❓ ¿Cómo actualizo la versión?

Edita `build.gradle.kts`:
```kotlin
version = "1.0.1"  // Cambiar aquí

// ...

nativeDistributions {
    packageVersion = "1.0.1"  // Y aquí
}
```

---

## ❓ ¿Cómo cambio el icono de la aplicación?

1. Crea un archivo `.ico` (Windows) o `.icns` (macOS)
2. Colócalo en `src/jvmMain/resources/`
3. Edita `build.gradle.kts`:

```kotlin
windows {
    iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
}
```

Herramientas para crear iconos:
- **Online:** https://icoconvert.com/
- **Software:** GIMP, Paint.NET, Photoshop

---

## ❓ ¿Puedo generar para Linux o macOS desde Windows?

**No directamente.** jpackage solo puede generar para el SO en el que se ejecuta:
- Windows → EXE/MSI
- macOS → DMG/PKG
- Linux → DEB/RPM

Para compilar para todos los SOs necesitas:
1. **CI/CD:** GitHub Actions, GitLab CI (recomendado)
2. **VMs:** Una máquina virtual por cada SO
3. **Cross-compilation:** Muy complejo, no soportado oficialmente

---

## ❓ ¿El ejecutable funciona en Windows 7/8?

Depende de:
- **JDK usado:** JDK 17+ requiere Windows 10+
- **Tu código:** APIs específicas de Windows 10/11

**Recomendación:** 
- Windows 10 o superior es el target mínimo recomendado
- Windows 11 tiene mejor soporte

---

## ❓ ¿Cómo distribuyo mi aplicación?

Opciones:

### 1. Descarga Directa
- Sube el EXE a tu sitio web
- Usuarios descargan e instalan

### 2. Repositorios
- **GitHub Releases:** Gratis, ideal para open source
- **Microsoft Store:** Requiere cuenta de desarrollador ($19)

### 3. Instaladores
- **Inno Setup:** Crear instalador más elaborado
- **NSIS:** Alternativa a Inno Setup

### 4. Auto-actualización
- Implementa sistema de updates
- Librerías: Update4j, AppUpdater

---

## ❓ ¿Qué pasa si cambio de JDK después?

El `gradle.properties` está configurado para usar JDK 21:
```properties
org.gradle.java.home=C:\\JAVA\\jdk-21.0.9
```

Si cambias o eliminas ese JDK:
1. Actualiza la ruta en `gradle.properties`
2. O bórrala para usar `JAVA_HOME` global

---

## ❓ Error: "module not found" al ejecutar

Posibles causas:
1. **Falta dependencia:** Verifica `build.gradle.kts`
2. **Recursos faltantes:** Verifica `src/jvmMain/resources/`
3. **Módulos Java:** Problemas con JPMS

Solución:
```powershell
.\gradlew.bat clean build packageExe
```

---

## ❓ ¿Puedo crear un ZIP portátil sin instalador?

Sí, hay tareas para eso:

```powershell
# Crear imagen de aplicación (no instalador)
.\gradlew.bat createDistributable

# Salida en:
build\compose\binaries\main\app\
```

Luego comprime esa carpeta en ZIP.

---

## ❓ ¿Dónde se instala la aplicación?

Por defecto:
- **Usuario:** `C:\Users\<usuario>\AppData\Local\KodeForge`
- **Sistema:** `C:\Program Files\KodeForge` (requiere admin)

Configurable en `build.gradle.kts`:
```kotlin
windows {
    perUserInstall = true   // false para instalar en Program Files
}
```

---

## 📞 ¿Más ayuda?

- **Documentación oficial:** https://github.com/JetBrains/compose-multiplatform
- **Compose Desktop:** https://www.jetbrains.com/lp/compose-multiplatform/
- **jpackage docs:** https://docs.oracle.com/en/java/javase/17/jpackage/
