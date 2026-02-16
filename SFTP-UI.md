# SFTP — UI Conexiones

## Objetivo

Implementar la UI del tool SFTP para gestionar conexiones SFTP/SSH, siguiendo el estilo visual de las otras herramientas (DB Tools, REST/SOAP, SMTP Fake).

## Implementación

### 1. Componentes de UI

#### SftpConnectionItem

**Archivo:** `src/commonMain/kotlin/com/kodeforge/ui/components/SftpConnectionItem.kt`

**Propósito:** Componente para mostrar un item de conexión SFTP en una lista.

**Características:**
- ✅ Diseño en Card con elevación
- ✅ Muestra información de la conexión:
  - Nombre (bold)
  - Host y puerto
  - Usuario
  - Badge de tipo de autenticación (Password/Key/None)
- ✅ Botones de acción:
  - Editar (icono Edit, color azul)
  - Eliminar (icono Delete, color rojo)
- ✅ Badges con colores diferenciados:
  - Password: azul (`#E3F2FD` / `#1976D2`)
  - Key: naranja (`#FFF3E0` / `#F57C00`)
  - None: gris (`#E0E0E0` / `#616161`)

#### SftpConnectionForm

**Archivo:** `src/commonMain/kotlin/com/kodeforge/ui/components/SftpConnectionForm.kt`

**Propósito:** Formulario para crear o editar una conexión SFTP.

**Características:**
- ✅ Diseño en Card con elevación
- ✅ Título dinámico: "Nueva Conexión SFTP" o "Editar Conexión SFTP"
- ✅ Campos del formulario:
  - **Nombre** (obligatorio)
  - **Host** (obligatorio, con placeholder)
  - **Puerto** (obligatorio, default: 22)
  - **Usuario** (obligatorio)
  - **Tipo de Autenticación** (dropdown: Password/SSH Key/None)
  - **Referencia al Secreto** (obligatorio si no es "none", con placeholder)
- ✅ Validación en tiempo real:
  - Nombre no vacío
  - Host no vacío
  - Puerto válido (1-65535)
  - Usuario no vacío
  - ValueRef obligatorio si auth type no es "none"
- ✅ Mensajes de error inline
- ✅ Botones:
  - Cancelar (outlined)
  - Guardar (habilitado solo si validación pasa)
- ✅ Scroll vertical para campos largos
- ✅ Ancho máximo de 600dp para mejor legibilidad

**Validaciones implementadas:**
```kotlin
- nameError: "El nombre es obligatorio"
- hostError: "El host es obligatorio"
- portError: "Puerto inválido" / "Puerto debe estar entre 1 y 65535"
- usernameError: "El usuario es obligatorio"
- authValueRefError: "La referencia al secreto es obligatoria"
```

#### SftpToolScreen

**Archivo:** `src/commonMain/kotlin/com/kodeforge/ui/screens/SftpToolScreen.kt`

**Propósito:** Pantalla principal del tool SFTP.

**Características:**

##### Header
- ✅ Título: "Conexiones SFTP"
- ✅ Subtítulo: "Gestiona tus conexiones SFTP/SSH"
- ✅ Toggle enabled/disabled con Switch
- ✅ Indicador de estado (Habilitado/Deshabilitado) con color
- ✅ Elevación con sombra

##### Estado Vacío
- ✅ Mensaje: "No hay conexiones SFTP"
- ✅ Submensaje: "Crea tu primera conexión para comenzar"
- ✅ Botón "Nueva Conexión" con icono

##### Lista de Conexiones
- ✅ LazyColumn con scroll
- ✅ Espaciado de 12dp entre items
- ✅ Padding de 24dp
- ✅ FloatingActionButton (FAB) para añadir conexión
  - Posición: Bottom-End
  - Color: azul (`#1976D2`)
  - Icono: Add

##### Formulario
- ✅ Se muestra en overlay cuando se crea o edita
- ✅ Centrado en la pantalla
- ✅ Ancho máximo de 600dp

##### Diálogo de Eliminación
- ✅ Título: "Eliminar Conexión"
- ✅ Mensaje de confirmación con nombre de la conexión
- ✅ Botones:
  - Cancelar (outlined)
  - Eliminar (rojo, `#D32F2F`)

### 2. Integración en ToolScreen

**Archivo:** `src/commonMain/kotlin/com/kodeforge/ui/screens/ToolScreen.kt`

**Cambios:**
- ✅ Añadido bloque condicional para `toolType == "sftp"`
- ✅ Renderiza `SftpToolScreen` cuando se selecciona el tool SFTP
- ✅ Pasa `workspace`, `projectId`, `onWorkspaceUpdate` y `modifier`

```kotlin
// Si es "sftp", renderizar SftpToolScreen
if (toolType == "sftp") {
    SftpToolScreen(
        workspace = workspace,
        projectId = project.id,
        onWorkspaceUpdate = onWorkspaceUpdate,
        modifier = modifier
    )
    return
}
```

### 3. Flujo de Usuario

#### Crear Conexión
1. Usuario hace clic en "Nueva Conexión" (FAB o botón en estado vacío)
2. Se muestra el formulario vacío
3. Usuario completa los campos
4. Validación en tiempo real muestra errores
5. Usuario hace clic en "Guardar"
6. Se llama a `useCases.addConnection()`
7. Si éxito: se actualiza el workspace y se cierra el formulario
8. Si error: se muestra en consola (TODO: snackbar)

#### Editar Conexión
1. Usuario hace clic en el icono "Editar" de una conexión
2. Se muestra el formulario pre-rellenado con los datos de la conexión
3. Usuario modifica los campos
4. Usuario hace clic en "Guardar"
5. Se llama a `useCases.updateConnection()`
6. Si éxito: se actualiza el workspace y se cierra el formulario

#### Eliminar Conexión
1. Usuario hace clic en el icono "Eliminar" de una conexión
2. Se muestra diálogo de confirmación
3. Usuario confirma
4. Se llama a `useCases.deleteConnection()`
5. Se actualiza el workspace y se cierra el diálogo

#### Habilitar/Deshabilitar Tool
1. Usuario cambia el Switch en el header
2. Se llama a `useCases.enableSftpTool()` o `useCases.disableSftpTool()`
3. Se actualiza el workspace
4. El indicador de estado cambia de color

### 4. Persistencia

✅ **Todas las operaciones persisten en workspace JSON:**
- Crear conexión → `useCases.addConnection()` → actualiza workspace
- Editar conexión → `useCases.updateConnection()` → actualiza workspace
- Eliminar conexión → `useCases.deleteConnection()` → actualiza workspace
- Habilitar/deshabilitar → `useCases.enableSftpTool()` / `disableSftpTool()` → actualiza workspace

El callback `onWorkspaceUpdate(updatedWorkspace)` propaga los cambios al estado principal de la aplicación, que se serializa automáticamente al JSON.

### 5. Consistencia Visual

La UI de SFTP sigue el mismo patrón que las otras herramientas:

#### Colores
- ✅ Primario: `#1976D2` (azul)
- ✅ Error: `#D32F2F` (rojo)
- ✅ Éxito: `#4CAF50` (verde)
- ✅ Texto primario: `#212121`
- ✅ Texto secundario: `#757575`
- ✅ Bordes: `#BDBDBD` / `#E0E0E0`
- ✅ Fondo: `Color.White`

#### Tipografía
- ✅ Título principal: 20sp, Bold
- ✅ Subtítulo: 14sp, Regular
- ✅ Nombre de conexión: 16sp, Bold
- ✅ Detalles: 14sp, Regular
- ✅ Badges: 11sp, Medium

#### Espaciado
- ✅ Padding de cards: 16dp
- ✅ Padding de formulario: 24dp
- ✅ Espaciado entre campos: 16dp
- ✅ Espaciado entre items: 12dp
- ✅ Padding de contenedor: 24dp

#### Componentes
- ✅ Cards con elevación de 2dp
- ✅ Formulario con elevación de 4dp
- ✅ Bordes redondeados (8dp)
- ✅ Botones con Material 3
- ✅ Switches con Material 3
- ✅ TextFields outlined con Material 3

### 6. Casos de Uso Soportados

#### Auth Types
- ✅ **Password:** Para autenticación con contraseña
- ✅ **SSH Key:** Para autenticación con clave privada
- ✅ **None:** Para conexiones sin autenticación

#### Validaciones
- ✅ Nombres únicos (validado en use cases)
- ✅ Campos obligatorios
- ✅ Puerto en rango válido (1-65535)
- ✅ ValueRef obligatorio si auth type no es "none"

#### Estados
- ✅ Estado vacío (sin conexiones)
- ✅ Lista de conexiones
- ✅ Formulario de creación
- ✅ Formulario de edición
- ✅ Diálogo de confirmación de eliminación

## NO Implementado (según requisitos)

- ❌ Conexión SFTP real
- ❌ Explorador de archivos remotos
- ❌ Navegación de directorios
- ❌ Transferencia de archivos
- ❌ Visualización de permisos
- ❌ Terminal SSH integrado

## Archivos Modificados

```
✅ src/commonMain/kotlin/com/kodeforge/ui/components/SftpConnectionItem.kt (nuevo)
✅ src/commonMain/kotlin/com/kodeforge/ui/components/SftpConnectionForm.kt (nuevo)
✅ src/commonMain/kotlin/com/kodeforge/ui/screens/SftpToolScreen.kt (nuevo)
✅ src/commonMain/kotlin/com/kodeforge/ui/screens/ToolScreen.kt (modificado)
```

## Resultados

### Compilación

```bash
./gradlew compileKotlinJvm
./gradlew build
```

**Resultado:** ✅ **BUILD SUCCESSFUL**

- ✅ Sin errores de compilación
- ✅ Sin errores de linting
- ✅ Todos los tests pasan (22 tests de SFTP previos)

### Características Implementadas

✅ **UI completa para SFTP:**
- Lista de conexiones con diseño consistente
- Formulario de creación/edición con validación
- Diálogo de confirmación de eliminación
- Toggle enabled/disabled
- Estado vacío amigable
- FAB para añadir conexiones

✅ **Validación en tiempo real:**
- Campos obligatorios
- Puerto válido
- ValueRef según auth type

✅ **Persistencia:**
- Todas las operaciones persisten en workspace JSON
- Cambios se propagan correctamente

✅ **Consistencia visual:**
- Colores coherentes con otras tools
- Tipografía consistente
- Espaciado uniforme
- Componentes Material 3

## Próximos Pasos Sugeridos

1. **Conexión SFTP real:** Implementar cliente SFTP multiplataforma
2. **Explorador de archivos:** UI para navegar directorios remotos
3. **Transferencia de archivos:** Upload/download de archivos
4. **Snackbar para errores:** Mostrar errores de forma más amigable
5. **Historial de conexiones:** Registrar últimas conexiones exitosas

---

**Fecha:** 2026-02-16  
**Compilación:** ✅ SUCCESS  
**Estado:** ✅ Completado

