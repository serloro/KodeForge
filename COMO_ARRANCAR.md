# KodeForge — Cómo Arrancar el Proyecto

**Fecha:** 2026-02-16  
**Versión:** 1.0

---

## 📋 Requisitos Previos

Antes de arrancar el proyecto, asegúrate de tener instalado:

### 1. **Java Development Kit (JDK) 17 o superior**

```bash
# Verificar versión de Java
java -version

# Debe mostrar algo como:
# openjdk version "17.0.x" o superior
```

**Instalación:**
- **macOS:** `brew install openjdk@17`
- **Linux:** `sudo apt install openjdk-17-jdk` (Ubuntu/Debian)
- **Windows:** Descargar desde [Adoptium](https://adoptium.net/)

### 2. **Gradle (opcional, el proyecto incluye wrapper)**

El proyecto incluye Gradle Wrapper (`gradlew`), por lo que NO es necesario instalar Gradle manualmente.

---

## 🚀 Pasos para Arrancar

### Paso 1: Clonar el Repositorio (si aplica)

```bash
git clone <url-del-repositorio>
cd kodeforge
```

### Paso 2: Verificar la Estructura del Proyecto

```
kodeforge/
├── build.gradle.kts          # Configuración de Gradle
├── gradle/                    # Gradle Wrapper
├── gradlew                    # Script de Gradle (Unix/macOS)
├── gradlew.bat                # Script de Gradle (Windows)
├── src/
│   ├── commonMain/            # Código común multiplataforma
│   │   └── kotlin/com/kodeforge/
│   │       ├── domain/        # Lógica de negocio
│   │       ├── data/          # Repositorios y persistencia
│   │       └── ui/            # Interfaz de usuario (Compose)
│   ├── jvmMain/               # Código específico de JVM/Desktop
│   │   └── kotlin/com/kodeforge/
│   │       └── ui/Main.kt     # Punto de entrada
│   └── jvmTest/               # Tests unitarios
├── data-schema.json           # Esquema de datos de ejemplo
└── specs/                     # Especificaciones y diseños
```

### Paso 3: Compilar el Proyecto

```bash
# En macOS/Linux:
./gradlew build

# En Windows:
gradlew.bat build
```

**Salida esperada:**
```
BUILD SUCCESSFUL in Xs
```

Si hay errores de compilación, revisa:
- Versión de Java (debe ser 17+)
- Conexión a internet (para descargar dependencias)
- Permisos de ejecución del gradlew: `chmod +x gradlew`

### Paso 4: Ejecutar la Aplicación

```bash
# En macOS/Linux:
./gradlew run

# En Windows:
gradlew.bat run
```

**Salida esperada:**
- Se abrirá una ventana de escritorio con la aplicación KodeForge
- Verás el sidebar con "Projects" y "Personas"
- El header con el logo "KodeForge" y botón "Nuevo Proyecto"

### Paso 5: Verificar que Todo Funciona

1. **Crear un Proyecto:**
   - Click en "Nuevo Proyecto" en el header
   - Completa el formulario
   - Guarda

2. **Crear una Persona:**
   - Click en "Gestionar" en la sección "Personas" del sidebar
   - Click en "Nueva Persona"
   - Completa `displayName` y `hoursPerDay`
   - Guarda

3. **Crear una Tarea:**
   - Selecciona un proyecto del sidebar
   - En la vista del proyecto, crea una tarea
   - Asígnala a una persona
   - Guarda

4. **Verificar Persistencia:**
   - Cierra la aplicación
   - Vuelve a ejecutar `./gradlew run`
   - Verifica que tus datos siguen ahí

---

## 🧪 Ejecutar Tests

```bash
# Ejecutar todos los tests:
./gradlew test

# Ver reporte de tests:
./gradlew test --info

# Reporte HTML:
# Se genera en: build/reports/tests/test/index.html
```

**Tests importantes:**
- `PersonUseCasesTest` - Validación de personas
- `TaskUseCasesTest` - Validación de tareas
- `ProjectUseCasesTest` - Validación de proyectos
- `SchedulerHardeningTest` - Validación del scheduler
- `*PortabilityTest` - Validación de persistencia JSON

---

## 📂 Ubicación de Datos

Los datos de la aplicación se guardan en:

```
~/.kodeforge/workspace.json
```

**Estructura del archivo:**
```json
{
  "metadata": {
    "version": "1.0.0",
    "lastModified": "2026-02-16T10:30:00Z"
  },
  "people": [...],
  "projects": [...],
  "tasks": [...],
  "planning": {
    "scheduleBlocks": [...]
  },
  "secrets": {...}
}
```

**Para resetear la aplicación:**
```bash
rm ~/.kodeforge/workspace.json
```

---

## 🛠️ Comandos Útiles

### Limpiar Build

```bash
./gradlew clean
```

### Compilar sin Tests

```bash
./gradlew build -x test
```

### Ejecutar con Logs de Debug

```bash
./gradlew run --debug
```

### Generar Distribución Ejecutable

```bash
./gradlew packageDistributionForCurrentOS
```

El ejecutable se generará en:
```
build/compose/binaries/main/app/KodeForge/
```

### Ver Dependencias

```bash
./gradlew dependencies
```

---

## 🎨 Verificar Refinamiento Visual

Después de arrancar, verifica que los cambios visuales estén aplicados:

### ✅ Checklist Visual

**Sidebar:**
- [ ] Ancho: 240px
- [ ] Fondo: #F7F8FA (gris muy claro)
- [ ] Items: altura 40px
- [ ] Spacing entre items: 8px
- [ ] Selected: fondo azul claro + borde izquierdo 3px

**Header:**
- [ ] Altura: 64px
- [ ] Logo "K" en cuadrado azul de 32px
- [ ] Botón "Nuevo Proyecto" con fondo azul #2563EB

**Colores:**
- [ ] Azul primario: #2563EB (más oscuro)
- [ ] Verde éxito: #10B981 (más vibrante)
- [ ] Rojo error: #EF4444 (más vibrante)
- [ ] Grises: escala de 50 a 900

**Tipografía:**
- [ ] Títulos: 24sp bold
- [ ] Cuerpo: 14sp regular
- [ ] Números grandes: 32sp bold

**Timeline (en vista proyecto):**
- [ ] Filas: 40px altura
- [ ] Bloques: bordes redondeados 4px
- [ ] Verde: #10B981
- [ ] Rojo: #EF4444
- [ ] Línea "Hoy": azul 2px ancho

---

## 🐛 Solución de Problemas

### Error: "Java version is too old"

**Solución:**
```bash
# Verificar versión
java -version

# Si es menor a 17, instalar JDK 17+
brew install openjdk@17  # macOS
```

### Error: "Permission denied: ./gradlew"

**Solución:**
```bash
chmod +x gradlew
./gradlew build
```

### Error: "Could not resolve dependencies"

**Solución:**
```bash
# Limpiar caché de Gradle
rm -rf ~/.gradle/caches/

# Volver a compilar
./gradlew clean build --refresh-dependencies
```

### La aplicación no guarda datos

**Verificar:**
1. Permisos de escritura en `~/.kodeforge/`
2. Logs de la aplicación: `./gradlew run --info`
3. Verificar que `workspace.json` se crea correctamente

**Solución:**
```bash
# Crear directorio manualmente
mkdir -p ~/.kodeforge
chmod 755 ~/.kodeforge
```

### La UI no se ve como en las specs

**Verificar:**
1. Que los cambios visuales se compilaron: `./gradlew clean build`
2. Que no hay errores de compilación: `./gradlew build --info`
3. Que la versión de Compose es correcta: revisar `build.gradle.kts`

---

## 📚 Recursos Adicionales

### Documentación del Proyecto

- `specs/spec.md` - Especificación funcional completa
- `specs/ui.md` - Especificación de UI
- `specs/data-schema.json` - Esquema de datos
- `specs/p1.png` - Diseño de Home/Dashboard
- `specs/p2.png` - Diseño de Vista Proyecto
- `UI-REFINEMENT-PLAN.md` - Plan de refinamiento visual

### Documentación de Implementación

- `T1-IMPLEMENTATION.md` - Implementación de workspace
- `SCHEDULER-HARDENING-COMPLETED.md` - Hardening del scheduler
- `TOTAL-PORTABILITY-ANALYSIS.md` - Análisis de portabilidad
- `AUDITORIA-FINAL.md` - Auditoría completa del proyecto

### Tests de Validación

- `*PortabilityTest.kt` - Tests de persistencia JSON
- `SchedulerHardeningTest.kt` - Tests del scheduler
- `*UseCasesTest.kt` - Tests de casos de uso

---

## 🎯 Próximos Pasos

Una vez que la aplicación esté corriendo:

1. **Explorar la UI:**
   - Crear proyectos, personas y tareas
   - Asignar tareas a personas
   - Ver el timeline del proyecto

2. **Probar el Scheduler:**
   - Asignar múltiples tareas a una persona
   - Verificar que el scheduler distribuye las horas correctamente
   - Ver la planificación en el timeline

3. **Probar las Herramientas:**
   - Tool: Info (páginas wiki)
   - Tool: REST/SOAP (cliente + mock server)
   - Tool: SMTP Fake (captura de emails)
   - Tool: DB Tools (consultas SQL)
   - Tool: SFTP (explorador remoto)

4. **Validar Portabilidad:**
   - Copiar `~/.kodeforge/workspace.json` a otro equipo
   - Verificar que todo se recupera correctamente

---

## 📞 Soporte

Si encuentras problemas:

1. Revisa la sección "Solución de Problemas" arriba
2. Ejecuta los tests: `./gradlew test`
3. Revisa los logs: `./gradlew run --info`
4. Consulta la documentación en `specs/` y `*.md`

---

**¡Listo para empezar! 🚀**

```bash
./gradlew run
```

