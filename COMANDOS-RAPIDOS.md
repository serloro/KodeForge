# Comandos Rápidos - KodeForge

## 🚀 Generar Ejecutables

### Windows EXE (instalador ejecutable)
```powershell
.\gradlew.bat packageExe
```
**Salida:** `build\compose\binaries\main\exe\KodeForge-1.0.0.exe`

### Windows MSI (instalador)
```powershell
.\gradlew.bat packageMsi
```
**Salida:** `build\compose\binaries\main\msi\KodeForge-1.0.0.msi`
**Requiere:** WiX Toolset instalado

### Todos los formatos para tu SO
```powershell
.\gradlew.bat packageDistributionForCurrentOS
```

---

## 🔧 Desarrollo

### Ejecutar la aplicación
```powershell
.\gradlew.bat run
```

### Compilar sin empaquetar
```powershell
.\gradlew.bat build
```

### Limpiar build
```powershell
.\gradlew.bat clean
```

### Limpiar y compilar
```powershell
.\gradlew.bat clean build
```

---

## 🧪 Testing

### Ejecutar todos los tests
```powershell
.\gradlew.bat test
```

### Ejecutar tests con reporte detallado
```powershell
.\gradlew.bat test --info
```

---

## 📊 Información

### Ver todas las tareas disponibles
```powershell
.\gradlew.bat tasks
```

### Ver tareas de Compose
```powershell
.\gradlew.bat tasks --group=compose
```

### Ver dependencias
```powershell
.\gradlew.bat dependencies
```

### Verificar versión de Gradle
```powershell
.\gradlew.bat --version
```

---

## 🔍 Debug

### Ejecutar con stacktrace
```powershell
.\gradlew.bat packageExe --stacktrace
```

### Ejecutar con más información
```powershell
.\gradlew.bat packageExe --info
```

### Ejecutar con debug completo
```powershell
.\gradlew.bat packageExe --debug
```

---

## 🛑 Detener Gradle Daemon

Si Gradle se queda colgado:
```powershell
.\gradlew.bat --stop
```

---

## 📂 Verificar Salida

### Listar archivos generados
```powershell
Get-ChildItem build\compose\binaries\main -Recurse | Select-Object FullName
```

### Ver tamaño de los ejecutables
```powershell
Get-ChildItem build\compose\binaries\main -Recurse -File | Select-Object Name, @{N='Size(MB)';E={[math]::Round($_.Length/1MB,2)}}
```

---

## 🌐 Variables de Entorno Útiles

### Ver JAVA_HOME
```powershell
echo $env:JAVA_HOME
```

### Ver PATH
```powershell
$env:PATH -split ';' | Select-String java
```

### Verificar jpackage
```powershell
jpackage --version
```

---

## 💡 Tips

1. **Primera compilación:** Puede tomar 5-10 minutos (descarga dependencias)
2. **Compilaciones siguientes:** 2-5 minutos
3. **Tamaño del EXE:** ~150-250 MB (incluye JVM)
4. **Requisitos:** JDK 14+ con jpackage

---

## 🆘 Ayuda Rápida

**Error de jpackage:** Ver `SOLUCION-JPACKAGE.md`
**Guía completa:** Ver `GENERAR-EXE-WINDOWS.md`
**Estado actual:** Ver `RESUMEN-SOLUCION.md`
