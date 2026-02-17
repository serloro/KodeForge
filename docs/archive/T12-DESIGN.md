# T12 - Validación Portable (Info) - Diseño

**Objetivo:** Validar que la persistencia de páginas Info es portable.

**Alcance:** Test de load → modify → save → reload → assert.

---

## 📋 ANÁLISIS

### **Requisitos:**

1. Validar que páginas Info se recuperan exactamente igual
2. Validar que idiomas (es/en) se preservan
3. Validar que HTML se preserva exactamente
4. Test: load → modify → save → reload → assert

### **NO implementar:**

- ❌ Nuevas features
- ❌ Cambios en la UI
- ❌ Cambios en los use cases

---

## 🧪 ESTRATEGIA DE VALIDACIÓN

### **Test de Persistencia Portable:**

```kotlin
@Test
fun `portable persistence - info pages survive save and reload cycle`() {
    // 1. LOAD: Crear workspace con páginas Info
    val workspace1 = createWorkspaceWithInfoPages()
    
    // 2. SAVE: Guardar a JSON
    val json = serializeWorkspace(workspace1)
    
    // 3. RELOAD: Cargar desde JSON
    val workspace2 = deserializeWorkspace(json)
    
    // 4. ASSERT: Verificar igualdad exacta
    assertInfoPagesEqual(workspace1, workspace2)
}
```

### **Validaciones Específicas:**

1. **Número de páginas:** Mismo número de páginas
2. **IDs:** Mismos IDs de páginas
3. **Slugs:** Mismos slugs
4. **Títulos:** Mismos títulos en ambos idiomas
5. **Order:** Mismo orden
6. **HTML:** Mismo HTML en ambos idiomas
7. **Timestamps:** Mismos timestamps

---

## 🧪 TESTS A CREAR

### **InfoPortabilityTest.kt:**

```kotlin
class InfoPortabilityTest {
    
    @Test
    fun `portable persistence - info pages survive save and reload`()
    
    @Test
    fun `portable persistence - html content preserved exactly`()
    
    @Test
    fun `portable persistence - multiple languages preserved`()
    
    @Test
    fun `portable persistence - page order preserved`()
    
    @Test
    fun `portable persistence - timestamps preserved`()
    
    @Test
    fun `portable persistence - modify html and reload`()
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/jvmTest/kotlin/com/kodeforge/InfoPortabilityTest.kt`**
   - Tests de persistencia portable
   - Validación de load/save/reload

---

## 📁 ARCHIVOS A MODIFICAR

Ninguno (solo tests).

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Test |
|-----------|------|
| Páginas Info se recuperan | `info pages survive save and reload` |
| HTML se preserva exactamente | `html content preserved exactly` |
| Idiomas se preservan | `multiple languages preserved` |
| Orden se preserva | `page order preserved` |
| Timestamps se preservan | `timestamps preserved` |
| Modify → Save → Reload | `modify html and reload` |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `InfoPortabilityTest.kt`
2. ✅ Test: páginas sobreviven ciclo save/reload
3. ✅ Test: HTML se preserva exactamente
4. ✅ Test: múltiples idiomas se preservan
5. ✅ Test: orden se preserva
6. ✅ Test: timestamps se preservan
7. ✅ Test: modificar HTML y recargar
8. ✅ Ejecutar tests
9. ✅ Validar que todos pasan

---

**Tiempo estimado:** 1 hora  
**Complejidad:** Media  
**Dependencias:** WorkspaceRepository, InfoUseCases

---

*Diseño completado - Listo para implementación*

