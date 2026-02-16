# KodeForge — Plan de Refinamiento Visual

**Fecha:** 2026-02-16  
**Objetivo:** Refinar UI para coincidir con specs/p1.png y specs/p2.png  
**Restricción:** NO tocar lógica, solo visual

---

## ANÁLISIS DE SPECS

### p1.png (Home/Dashboard)
- **Sidebar:** ~240px ancho, fondo gris muy claro (#F7F8FA)
- **Header:** 64px altura, fondo blanco, sombra sutil
- **Cards de métricas:** 4 columnas, fondo blanco, bordes redondeados, sombra sutil
- **Spacing:** 16px entre cards, 24px padding interno
- **Tipografía:**
  - Títulos de sección: 24px, bold
  - Títulos de cards: 14px, medium
  - Números grandes: 32px, bold
  - Subtítulos: 12px, regular, gris
- **Colores:**
  - Azul primario: #2563EB
  - Verde éxito: #10B981
  - Rojo error: #EF4444
  - Naranja warning: #F59E0B
  - Gris texto: #6B7280
  - Gris claro: #F3F4F6

### p2.png (Project View)
- **Timeline:**
  - Filas: 40px altura cada una
  - Bloques: bordes redondeados 4px
  - Verde: tareas normales (#10B981)
  - Rojo: excedidos (#EF4444)
  - Gris claro: capacidad libre (#E5E7EB)
  - Línea "Hoy": azul (#2563EB), 2px ancho, muy visible
- **Utility tiles:** 6 columnas, iconos centrados, 80px altura
- **Spacing:** 12px entre filas, 16px entre tiles

---

## COMPONENTES A REFINAR

### 1. KodeForgeColors.kt (Sistema de colores)

**Actual:**
```kotlin
object KodeForgeColors {
    val Primary = Color(0xFF2196F3)
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF4CAF50)
    // ...
}
```

**Refinado:**
```kotlin
object KodeForgeColors {
    // Primarios
    val Primary = Color(0xFF2563EB)        // Azul más oscuro
    val PrimaryLight = Color(0xFF3B82F6)
    val PrimaryDark = Color(0xFF1D4ED8)
    
    // Estados
    val Success = Color(0xFF10B981)        // Verde más vibrante
    val Error = Color(0xFFEF4444)          // Rojo más vibrante
    val Warning = Color(0xFFF59E0B)        // Naranja
    
    // Grises (escala completa)
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
    
    // Fondos
    val Background = Color(0xFFFFFFFF)
    val BackgroundSecondary = Color(0xFFF7F8FA)
    val Surface = Color(0xFFFFFFFF)
    
    // Texto
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
}
```

---

### 2. KodeForgeTypography.kt (Sistema tipográfico)

**Refinado:**
```kotlin
object KodeForgeTypography {
    // Títulos de página
    val H1 = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    val H2 = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    val H3 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    // Subtítulos
    val Subtitle1 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    val Subtitle2 = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        color = KodeForgeColors.TextSecondary
    )
    
    // Cuerpo
    val Body1 = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    val Body2 = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = KodeForgeColors.TextSecondary
    )
    
    // Caption
    val Caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = KodeForgeColors.TextTertiary
    )
    
    // Números grandes (métricas)
    val MetricLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        color = KodeForgeColors.TextPrimary
    )
    
    val MetricMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        color = KodeForgeColors.TextPrimary
    )
}
```

---

### 3. KodeForgeSpacing.kt (Sistema de espaciado)

**Nuevo archivo:**
```kotlin
object KodeForgeSpacing {
    val XXS = 4.dp
    val XS = 8.dp
    val SM = 12.dp
    val MD = 16.dp
    val LG = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
    
    // Específicos
    val CardPadding = MD
    val CardSpacing = MD
    val SectionSpacing = LG
    val TimelineRowHeight = 40.dp
    val TimelineRowSpacing = SM
    val SidebarWidth = 240.dp
    val HeaderHeight = 64.dp
}
```

---

### 4. Sidebar.kt

**Cambios:**
- Ancho: 200dp → 240dp
- Fondo: Gray100 → BackgroundSecondary (#F7F8FA)
- Padding: 8dp → 12dp
- Spacing entre items: 4dp → 8dp
- Hover: más sutil

**Código:**
```kotlin
@Composable
fun Sidebar(...) {
    Surface(
        modifier = Modifier
            .width(KodeForgeSpacing.SidebarWidth) // 240dp
            .fillMaxHeight(),
        color = KodeForgeColors.BackgroundSecondary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KodeForgeSpacing.SM) // 12dp
        ) {
            // Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = KodeForgeSpacing.MD), // 16dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = KodeForgeColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(KodeForgeSpacing.SM))
                Text(
                    "KodeForge",
                    style = KodeForgeTypography.H3
                )
            }
            
            Spacer(Modifier.height(KodeForgeSpacing.LG)) // 24dp
            
            // Secciones
            SidebarSection(
                title = "Proyectos",
                items = projects,
                selectedId = selectedProjectId,
                onItemClick = onProjectClick,
                onManageClick = onManageProjectsClick
            )
            
            Spacer(Modifier.height(KodeForgeSpacing.LG)) // 24dp
            
            SidebarSection(
                title = "Personas",
                items = people,
                selectedId = selectedPersonId,
                onItemClick = onPersonClick,
                onManageClick = onManagePeopleClick
            )
        }
    }
}
```

---

### 5. SidebarSection.kt

**Cambios:**
- Título: 12sp → 14sp, medium
- Spacing: 4dp → 8dp
- Botón "Gestionar": más compacto

**Código:**
```kotlin
@Composable
fun SidebarSection(...) {
    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KodeForgeSpacing.XS), // 8dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = KodeForgeTypography.Subtitle2, // 14sp medium
                color = KodeForgeColors.TextSecondary
            )
            
            IconButton(
                onClick = onManageClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Gestionar",
                    tint = KodeForgeColors.Gray400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
        
        // Items
        items.forEach { item ->
            SidebarItem(
                item = item,
                isSelected = item.id == selectedId,
                onClick = { onItemClick(item.id) }
            )
            Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
        }
    }
}
```

---

### 6. ProjectItem.kt / PersonItem.kt

**Cambios:**
- Altura: 36dp → 40dp
- Padding: 8dp → 12dp
- Hover: fondo Gray100 (más sutil)
- Selected: fondo Primary con alpha 0.1
- Borde izquierdo: 3px cuando selected

**Código:**
```kotlin
@Composable
fun ProjectItem(
    project: Project,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> KodeForgeColors.Primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val hoverBackgroundColor = if (isSelected) {
        backgroundColor
    } else {
        KodeForgeColors.Gray100
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .hoverable(
                onHover = { /* cambiar fondo */ }
            )
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KodeForgeSpacing.SM), // 12dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Borde izquierdo si selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .background(
                            KodeForgeColors.Primary,
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(Modifier.width(KodeForgeSpacing.XS))
            }
            
            // Icono
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = if (isSelected) KodeForgeColors.Primary else KodeForgeColors.Gray400,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
            
            // Nombre
            Text(
                project.name,
                style = KodeForgeTypography.Body1,
                color = if (isSelected) KodeForgeColors.Primary else KodeForgeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

---

### 7. Header.kt

**Cambios:**
- Altura: 56dp → 64dp
- Padding: 16dp → 24dp horizontal
- Sombra: más sutil (elevation 1dp)
- Título: H2 (24sp bold)

**Código:**
```kotlin
@Composable
fun Header(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(KodeForgeSpacing.HeaderHeight), // 64dp
        color = KodeForgeColors.Surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KodeForgeSpacing.LG), // 24dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    title,
                    style = KodeForgeTypography.H2 // 24sp bold
                )
                
                if (subtitle != null) {
                    Spacer(Modifier.height(KodeForgeSpacing.XXS)) // 4dp
                    Text(
                        subtitle,
                        style = KodeForgeTypography.Caption,
                        color = KodeForgeColors.TextSecondary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(KodeForgeSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
    }
}
```

---

### 8. MetricCard.kt (Cards de métricas en Home)

**Nuevo componente:**
```kotlin
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = KodeForgeColors.Primary,
    iconBackground: Color = KodeForgeColors.Primary.copy(alpha = 0.1f)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = KodeForgeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KodeForgeSpacing.MD), // 16dp
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header con icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = KodeForgeTypography.Subtitle2,
                    color = KodeForgeColors.TextSecondary
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Valor
            Column {
                Text(
                    value,
                    style = KodeForgeTypography.MetricLarge // 32sp bold
                )
                
                if (subtitle != null) {
                    Spacer(Modifier.height(KodeForgeSpacing.XXS)) // 4dp
                    Text(
                        subtitle,
                        style = KodeForgeTypography.Caption,
                        color = KodeForgeColors.TextTertiary
                    )
                }
            }
        }
    }
}
```

---

### 9. UtilityTile.kt (Tiles en Project View)

**Cambios:**
- Altura: 72dp → 80dp
- Padding: 12dp → 16dp
- Icono: 24dp → 28dp
- Texto: 12sp → 14sp

**Código:**
```kotlin
@Composable
fun UtilityTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = KodeForgeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KodeForgeSpacing.MD), // 16dp
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
            
            Text(
                title,
                style = KodeForgeTypography.Body2, // 12sp
                color = KodeForgeColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

---

### 10. TaskBlock.kt (Bloques en Timeline)

**Cambios:**
- Altura: según hoursPlanned (8px por hora)
- Bordes redondeados: 4px
- Colores más vibrantes:
  - Normal: Success (#10B981)
  - Excedido: Error (#EF4444)
  - Libre: Gray200 (#E5E7EB)
- Padding interno: 4dp
- Texto: 11sp

**Código:**
```kotlin
@Composable
fun TaskBlock(
    task: Task,
    hoursPlanned: Double,
    isOverloaded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isOverloaded) {
        KodeForgeColors.Error
    } else {
        KodeForgeColors.Success
    }
    
    val textColor = Color.White
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((hoursPlanned * 8).dp) // 8px por hora
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(KodeForgeSpacing.XXS), // 4dp
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            task.title,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

---

### 11. TimelineRow.kt

**Cambios:**
- Altura: 36dp → 40dp
- Spacing: 8dp → 12dp entre bloques
- Nombre persona: 14sp medium
- Indicador de excedido: texto rojo + icono warning

**Código:**
```kotlin
@Composable
fun TimelineRow(
    person: Person,
    blocks: List<ScheduleBlock>,
    isOverloaded: Boolean,
    dateRange: ClosedRange<LocalDate>,
    onBlockClick: (ScheduleBlock) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(KodeForgeSpacing.TimelineRowHeight), // 40dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre persona (ancho fijo)
        Row(
            modifier = Modifier
                .width(180.dp)
                .padding(end = KodeForgeSpacing.MD), // 16dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isOverloaded) KodeForgeColors.Error.copy(alpha = 0.1f)
                        else KodeForgeColors.Gray200,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    person.displayName.first().toString(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverloaded) KodeForgeColors.Error
                                else KodeForgeColors.TextSecondary
                    )
                )
            }
            
            Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
            
            // Nombre
            Text(
                person.displayName,
                style = KodeForgeTypography.Body1,
                color = if (isOverloaded) KodeForgeColors.Error
                        else KodeForgeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Indicador de excedido
            if (isOverloaded) {
                Spacer(Modifier.width(KodeForgeSpacing.XXS)) // 4dp
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Excedido",
                    tint = KodeForgeColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Timeline (bloques)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Renderizar bloques...
        }
    }
}
```

---

### 12. TodayLine.kt (Línea "Hoy" en Timeline)

**Cambios:**
- Ancho: 1px → 2px
- Color: Primary más vibrante
- Sombra: más visible
- Label: fondo blanco, borde azul

**Código:**
```kotlin
@Composable
fun TodayLine(
    position: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = position)
            .width(2.dp)
            .fillMaxHeight()
            .background(KodeForgeColors.Primary)
            .shadow(2.dp)
    )
    
    // Label "Hoy"
    Box(
        modifier = Modifier
            .offset(x = position - 20.dp, y = (-32).dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, KodeForgeColors.Primary, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "Hoy",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = KodeForgeColors.Primary
            )
        )
    }
}
```

---

## RESUMEN DE CAMBIOS

### Colores
- ✅ Primary: #2196F3 → #2563EB (más oscuro)
- ✅ Success: #4CAF50 → #10B981 (más vibrante)
- ✅ Error: #D32F2F → #EF4444 (más vibrante)
- ✅ Añadir escala de grises completa (50-900)
- ✅ BackgroundSecondary: #F7F8FA

### Tipografía
- ✅ Títulos: 24sp bold (H2)
- ✅ Subtítulos: 14sp medium
- ✅ Cuerpo: 14sp regular
- ✅ Caption: 12sp regular
- ✅ Métricas: 32sp bold

### Spacing
- ✅ Sidebar: 200dp → 240dp
- ✅ Header: 56dp → 64dp
- ✅ Card padding: 16dp
- ✅ Card spacing: 16dp
- ✅ Section spacing: 24dp
- ✅ Timeline row: 40dp
- ✅ Timeline row spacing: 12dp

### Elevaciones
- ✅ Cards: 1dp (más sutil)
- ✅ Header: 1dp (más sutil)
- ✅ Hover: 2dp

### Bordes
- ✅ Cards: 12px redondeados
- ✅ Botones: 6px redondeados
- ✅ Task blocks: 4px redondeados
- ✅ Selected item: borde izquierdo 3px

### Estados
- ✅ Hover: Gray100 (más sutil)
- ✅ Selected: Primary con alpha 0.1
- ✅ Overloaded: Error (#EF4444) con icono warning

---

## ORDEN DE IMPLEMENTACIÓN

1. ✅ KodeForgeColors.kt (base)
2. ✅ KodeForgeTypography.kt (base)
3. ✅ KodeForgeSpacing.kt (nuevo)
4. ✅ Sidebar.kt + SidebarSection.kt
5. ✅ ProjectItem.kt + PersonItem.kt
6. ✅ Header.kt
7. ✅ MetricCard.kt (nuevo)
8. ✅ UtilityTile.kt
9. ✅ TaskBlock.kt
10. ✅ TimelineRow.kt
11. ✅ TodayLine.kt

**Tiempo estimado:** 2-3 horas  
**Riesgo:** BAJO (solo visual, no lógica)

