# AJUSTES VISUALES UI - Comparación con p1.png

## Diferencias Identificadas

### 🔴 CRÍTICAS (Impacto visual alto)

1. **Proyecto Seleccionado - Borde**
   - **p1.png**: Borde izquierdo grueso (4dp), azul
   - **Actual**: Borde completo (2dp) en los 4 lados
   - **Impacto**: Cambia significativamente el look de selección

2. **Avatar Persona - Indicador de Estado**
   - **p1.png**: Avatar neutral + punto de color separado
   - **Actual**: Avatar con color de fondo (verde/naranja/rojo)
   - **Impacto**: Menos claro visualmente, ocupa más espacio visual

3. **Botón "Gestionar" - Estilo**
   - **p1.png**: Muy discreto, casi texto plano con icono gris
   - **Actual**: OutlinedButton con borde visible
   - **Impacto**: Llama demasiado la atención

### 🟡 MODERADAS (Mejora la consistencia)

4. **Sidebar - Separación del Main Content**
   - **p1.png**: Sombra sutil o borde derecho
   - **Actual**: Sin separación visual
   - **Impacto**: Menos definición de áreas

5. **Avatar Persona - Tamaño**
   - **p1.png**: ~26px
   - **Actual**: 28dp
   - **Impacto**: Ligeramente más grande

### 🟢 MENORES (Refinamiento)

6. **Proyecto Item - Icono**
   - **p1.png**: Iconos variados, pequeños (~16-18px)
   - **Actual**: Círculo 20dp con inicial
   - **Impacto**: Mínimo, el enfoque actual es consistente

---

## Ajustes Recomendados (Sin nuevas funcionalidades)

### 1. ProjectItem - Borde Izquierdo en Selección

**Cambio:**
```kotlin
// ANTES: Borde completo
.border(
    width = if (isSelected) 2.dp else 0.dp,
    color = if (isSelected) KodeForgeColors.Primary else Color.Transparent,
    shape = RoundedCornerShape(8.dp)
)

// DESPUÉS: Borde izquierdo solamente
.drawBehind {
    if (isSelected) {
        drawLine(
            color = Primary,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 4.dp.toPx()
        )
    }
}
```

**Resultado:** Selección más sutil y profesional.

---

### 2. PersonItem - Avatar Neutral + Punto de Estado

**Cambio:**
```kotlin
// ANTES: Avatar con color de fondo
Box(
    modifier = Modifier.size(28.dp).clip(CircleShape).background(avatarColor)
) {
    Text(inicial, color = White)
}

// DESPUÉS: Avatar gris + punto de color separado
Row {
    // Punto de estado (8dp)
    Box(
        modifier = Modifier.size(8.dp).clip(CircleShape).background(avatarColor)
    )
    Spacer(Modifier.width(6.dp))
    
    // Avatar neutral
    Box(
        modifier = Modifier.size(26.dp).clip(CircleShape).background(Color(0xFFE0E0E0))
    ) {
        Text(inicial, color = Color(0xFF616161))
    }
    
    Spacer(Modifier.width(8.dp))
    Text(nombre)
}
```

**Resultado:** Estado más claro visualmente.

---

### 3. SidebarSection - Botón "Gestionar" Discreto

**Cambio:**
```kotlin
// ANTES: OutlinedButton
OutlinedButton(
    onClick = onManage,
    modifier = Modifier.height(28.dp)
) {
    Icon(Settings, size = 14.dp)
    Text("Gestionar")
}

// DESPUÉS: TextButton discreto
TextButton(
    onClick = onManage,
    modifier = Modifier.height(26.dp),
    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
    colors = ButtonDefaults.textButtonColors(
        contentColor = Color(0xFF757575)  // Gris medio
    )
) {
    Icon(Settings, size = 12.dp, tint = Color(0xFF9E9E9E))
    Spacer(Modifier.width(4.dp))
    Text("Gestionar", fontSize = 11.sp)
}
```

**Resultado:** Botón más discreto, no compite con el contenido.

---

### 4. Sidebar - Sombra de Separación

**Cambio:**
```kotlin
// ANTES: Sin sombra
Column(
    modifier = modifier
        .width(240.dp)
        .fillMaxHeight()
        .background(KodeForgeColors.SidebarBackground)
)

// DESPUÉS: Con elevation
Surface(
    modifier = modifier.width(240.dp).fillMaxHeight(),
    color = KodeForgeColors.SidebarBackground,
    shadowElevation = 1.dp
) {
    Column(...)
}
```

**Resultado:** Mejor separación visual del main content.

---

### 5. Tamaños - Ajustes Finos

**Cambios menores:**
```kotlin
// Avatar persona: 28dp → 26dp
.size(26.dp)

// Punto estado: nuevo, 8dp
.size(8.dp)

// Icono proyecto: 20dp → 18dp
.size(18.dp)

// Botón gestionar: 28dp → 26dp
.height(26.dp)
```

---

## Resumen Visual

### ANTES (Actual)
```
┌─────────────────────────┐
│ Projects   [Gestionar]  │ ← Botón outlined visible
├─────────────────────────┤
│ ●━ Cloud Scale UI ━━━━━ │ ← Borde completo azul
│ ● Data Pipeline 2.0     │
│ ● Legacy Migration      │
├─────────────────────────┤
│ Personas   [Gestionar]  │
├─────────────────────────┤
│ 🟢 Basso7               │ ← Avatar verde (estado en avatar)
│ 🟠 Blanco J             │ ← Avatar naranja
│ 🟢 Bocera J             │
└─────────────────────────┘
```

### DESPUÉS (Propuesto)
```
┌─────────────────────────┐
│ Projects   Gestionar    │ ← Botón discreto, gris
├─────────────────────────┤
│┃● Cloud Scale UI        │ ← Borde IZQUIERDO azul (4dp)
│ ● Data Pipeline 2.0     │
│ ● Legacy Migration      │
├─────────────────────────┤
│ Personas   Gestionar    │
├─────────────────────────┤
│ 🟢 ⚪ Basso7            │ ← Punto verde + avatar gris
│ 🟠 ⚪ Blanco J          │ ← Punto naranja + avatar gris
│ 🟢 ⚪ Bocera J          │ ← Punto verde + avatar gris
└─────────────────────────┘
```

---

## Implementación Sugerida

1. **ProjectItem.kt**: Cambiar border a drawBehind para borde izquierdo
2. **PersonItem.kt**: Avatar gris + punto de estado separado
3. **SidebarSection.kt**: TextButton en lugar de OutlinedButton
4. **Sidebar.kt**: Envolver en Surface con shadowElevation
5. **Ajustes de tamaño**: Reducir valores en 2dp donde indicado

---

## Validación Final

Después de aplicar los cambios, la UI debería:
- ✅ Tener borde izquierdo en proyecto seleccionado (como p1.png)
- ✅ Mostrar estado de persona con punto separado (más claro)
- ✅ Botón "Gestionar" discreto (no llama tanto la atención)
- ✅ Sidebar con separación sutil del main content
- ✅ Proporciones más cercanas a p1.png

**Impacto:** Mejora visual significativa sin cambiar funcionalidad.

