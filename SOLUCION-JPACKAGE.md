# SOLUCIÓN RÁPIDA - Error jpackage.exe

## ❌ PROBLEMA DETECTADO
Tu sistema tiene **JDK 11.0.16.1** instalado en `C:\JAVA\jdk-11.0.16.1`

**El error:** `'jpackage.exe' is missing`

**Causa:** JDK 11 NO incluye la herramienta `jpackage` que es necesaria para crear ejecutables (.exe/.msi)

---

## ✅ SOLUCIÓN

Necesitas **actualizar a JDK 14 o superior** (se recomienda JDK 17 o 21)

### Paso 1: Descargar JDK 17+
**Opción recomendada - Eclipse Temurin (gratis y libre):**
- Ir a: https://adoptium.net/
- Descargar: **JDK 17 LTS (x64)** para Windows (archivo .msi)
- Ejecutar el instalador

**Opción alternativa - Oracle JDK:**
- Ir a: https://www.oracle.com/java/technologies/downloads/
- Descargar JDK 17 o superior

### Paso 2: Configurar JAVA_HOME
Después de instalar el nuevo JDK:

```powershell
# Opción A: Configurar variable de entorno del usuario
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.XX', 'User')

# Opción B: Configurar variable de entorno del sistema (requiere admin)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.XX', 'Machine')
```

**IMPORTANTE:** Ajusta la ruta según donde se instaló el JDK

### Paso 3: Verificar PATH
Asegúrate de que `%JAVA_HOME%\bin` esté en el PATH:

```powershell
# Ver el PATH actual
$env:PATH -split ';' | Select-String java

# Si no aparece, agregarlo
$oldPath = [System.Environment]::GetEnvironmentVariable('PATH', 'User')
$newPath = "$oldPath;$env:JAVA_HOME\bin"
[System.Environment]::SetEnvironmentVariable('PATH', $newPath, 'User')
```

### Paso 4: REINICIAR la terminal
**Cierra y vuelve a abrir PowerShell/Terminal** para que los cambios surtan efecto

### Paso 5: Verificar que funciona
```powershell
# Verificar versión de Java (debe mostrar 17 o superior)
java -version

# Verificar JAVA_HOME
echo $env:JAVA_HOME

# Verificar que jpackage está disponible
jpackage --version
```

### Paso 6: Intentar generar el EXE nuevamente
```powershell
cd C:\Users\slromero\Downloads\KodeForge-main
.\gradlew.bat clean
.\gradlew.bat packageExe
```

---

## 🔄 ALTERNATIVA RÁPIDA (sin cambiar JAVA_HOME global)

Si no quieres cambiar tu JAVA_HOME global, puedes configurarlo solo para Gradle:

### Opción 1: Editar gradle.properties
Agregar al archivo `gradle.properties`:
```properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.XX
```

### Opción 2: Variable de entorno temporal
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.XX"
.\gradlew.bat packageExe
```

---

## 📋 CHECKLIST

- [ ] Descargar e instalar JDK 17+ desde https://adoptium.net/
- [ ] Configurar JAVA_HOME apuntando al nuevo JDK
- [ ] Reiniciar la terminal
- [ ] Ejecutar `java -version` (debe mostrar 17+)
- [ ] Ejecutar `jpackage --version` (debe funcionar)
- [ ] Ejecutar `.\gradlew.bat packageExe`

---

## 📖 Documentación Completa

Para más detalles, consulta: **GENERAR-EXE-WINDOWS.md**
