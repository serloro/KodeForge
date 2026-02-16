package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.InfoUseCases
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests de portabilidad para páginas Info.
 * 
 * Valida que al copiar el workspace JSON:
 * - Las páginas Info se recuperan exactamente igual
 * - Los idiomas (es/en) se preservan
 * - El HTML se preserva exactamente
 * - Los timestamps se preservan
 * - El orden se preserva
 * 
 * Flujo: load → modify → save → reload → assert
 */
class InfoPortabilityTest {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
    }
    
    private val infoUseCases = InfoUseCases()
    
    /**
     * Crea un workspace con páginas Info de prueba.
     */
    private fun createWorkspaceWithInfoPages(): Workspace {
        val project = Project(
            id = "proj_test",
            name = "Test Project",
            description = "Project for portability testing",
            status = "active",
            members = emptyList(),
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z",
            tools = ProjectTools(
                info = InfoTool(
                    enabled = true,
                    pages = listOf(
                        InfoPage(
                            id = "info_001",
                            slug = "intro",
                            title = mapOf(
                                "es" to "Introducción",
                                "en" to "Introduction"
                            ),
                            order = 1,
                            translations = mapOf(
                                "es" to InfoPageTranslation(
                                    html = "<h1>Introducción</h1><p>Bienvenido al proyecto.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
                                    updatedAt = "2026-02-16T10:00:00Z"
                                ),
                                "en" to InfoPageTranslation(
                                    html = "<h1>Introduction</h1><p>Welcome to the project.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
                                    updatedAt = "2026-02-16T10:05:00Z"
                                )
                            )
                        ),
                        InfoPage(
                            id = "info_002",
                            slug = "api-reference",
                            title = mapOf(
                                "es" to "Referencia API",
                                "en" to "API Reference"
                            ),
                            order = 2,
                            translations = mapOf(
                                "es" to InfoPageTranslation(
                                    html = "<h1>Referencia API</h1><h2>Endpoints</h2><p>Lista de endpoints disponibles:</p><ul><li><strong>GET</strong> /api/users</li><li><strong>POST</strong> /api/users</li></ul>",
                                    updatedAt = "2026-02-16T11:00:00Z"
                                ),
                                "en" to InfoPageTranslation(
                                    html = "<h1>API Reference</h1><h2>Endpoints</h2><p>Available endpoints:</p><ul><li><strong>GET</strong> /api/users</li><li><strong>POST</strong> /api/users</li></ul>",
                                    updatedAt = "2026-02-16T11:05:00Z"
                                )
                            )
                        ),
                        InfoPage(
                            id = "info_003",
                            slug = "faq",
                            title = mapOf(
                                "es" to "Preguntas Frecuentes",
                                "en" to "FAQ"
                            ),
                            order = 3,
                            translations = mapOf(
                                "es" to InfoPageTranslation(
                                    html = "<h1>Preguntas Frecuentes</h1><h2>¿Cómo empiezo?</h2><p>Sigue estos pasos:</p><ol><li>Instala las dependencias</li><li>Configura el proyecto</li><li>Ejecuta el servidor</li></ol>",
                                    updatedAt = "2026-02-16T12:00:00Z"
                                ),
                                "en" to InfoPageTranslation(
                                    html = "<h1>FAQ</h1><h2>How do I get started?</h2><p>Follow these steps:</p><ol><li>Install dependencies</li><li>Configure the project</li><li>Run the server</li></ol>",
                                    updatedAt = "2026-02-16T12:05:00Z"
                                )
                            )
                        )
                    )
                )
            )
        )
        
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T12:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(project),
            tasks = emptyList()
        )
    }
    
    @Test
    fun `portable persistence - info pages survive save and reload`() {
        // 1. LOAD: Crear workspace con páginas Info
        val workspace1 = createWorkspaceWithInfoPages()
        val project1 = workspace1.projects[0]
        val pages1 = project1.tools.info?.pages!!
        
        assertEquals(3, pages1.size)
        
        // 2. SAVE: Serializar a JSON
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar desde JSON
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val project2 = workspace2.projects[0]
        val pages2 = project2.tools.info?.pages!!
        
        // 4. ASSERT: Verificar igualdad
        assertEquals(pages1.size, pages2.size)
        
        for (i in pages1.indices) {
            val page1 = pages1[i]
            val page2 = pages2[i]
            
            assertEquals(page1.id, page2.id, "Page ID should match")
            assertEquals(page1.slug, page2.slug, "Page slug should match")
            assertEquals(page1.order, page2.order, "Page order should match")
            assertEquals(page1.title, page2.title, "Page titles should match")
            assertEquals(page1.translations.keys, page2.translations.keys, "Translation locales should match")
        }
    }
    
    @Test
    fun `portable persistence - html content preserved exactly`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithInfoPages()
        val page1 = workspace1.projects[0].tools.info?.pages?.get(0)!!
        
        val htmlEs1 = page1.translations["es"]?.html!!
        val htmlEn1 = page1.translations["en"]?.html!!
        
        // Verificar que el HTML tiene contenido complejo
        assert(htmlEs1.contains("<h1>"))
        assert(htmlEs1.contains("<p>"))
        assert(htmlEs1.contains("<ul>"))
        assert(htmlEs1.contains("<li>"))
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val page2 = workspace2.projects[0].tools.info?.pages?.get(0)!!
        
        val htmlEs2 = page2.translations["es"]?.html!!
        val htmlEn2 = page2.translations["en"]?.html!!
        
        // 4. ASSERT: HTML exactamente igual
        assertEquals(htmlEs1, htmlEs2, "Spanish HTML should be preserved exactly")
        assertEquals(htmlEn1, htmlEn2, "English HTML should be preserved exactly")
        
        // Verificar caracteres especiales
        assertEquals(
            "<h1>Introducción</h1><p>Bienvenido al proyecto.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
            htmlEs2
        )
    }
    
    @Test
    fun `portable persistence - multiple languages preserved`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithInfoPages()
        val pages1 = workspace1.projects[0].tools.info?.pages!!
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val pages2 = workspace2.projects[0].tools.info?.pages!!
        
        // 4. ASSERT: Ambos idiomas se preservan
        for (i in pages1.indices) {
            val page1 = pages1[i]
            val page2 = pages2[i]
            
            // Verificar que ambos idiomas existen
            assertNotNull(page2.translations["es"], "Spanish translation should exist")
            assertNotNull(page2.translations["en"], "English translation should exist")
            
            // Verificar títulos
            assertEquals(page1.title["es"], page2.title["es"], "Spanish title should match")
            assertEquals(page1.title["en"], page2.title["en"], "English title should match")
            
            // Verificar HTML
            assertEquals(
                page1.translations["es"]?.html,
                page2.translations["es"]?.html,
                "Spanish HTML should match"
            )
            assertEquals(
                page1.translations["en"]?.html,
                page2.translations["en"]?.html,
                "English HTML should match"
            )
        }
    }
    
    @Test
    fun `portable persistence - page order preserved`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithInfoPages()
        val pages1 = workspace1.projects[0].tools.info?.pages!!
        
        // Verificar orden inicial
        assertEquals(1, pages1[0].order)
        assertEquals(2, pages1[1].order)
        assertEquals(3, pages1[2].order)
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val pages2 = workspace2.projects[0].tools.info?.pages!!
        
        // 4. ASSERT: Orden preservado
        assertEquals(1, pages2[0].order)
        assertEquals(2, pages2[1].order)
        assertEquals(3, pages2[2].order)
        
        // Verificar slugs en orden correcto
        assertEquals("intro", pages2[0].slug)
        assertEquals("api-reference", pages2[1].slug)
        assertEquals("faq", pages2[2].slug)
    }
    
    @Test
    fun `portable persistence - timestamps preserved`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithInfoPages()
        val page1 = workspace1.projects[0].tools.info?.pages?.get(1)!! // API Reference
        
        val timestampEs1 = page1.translations["es"]?.updatedAt!!
        val timestampEn1 = page1.translations["en"]?.updatedAt!!
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val page2 = workspace2.projects[0].tools.info?.pages?.get(1)!!
        
        val timestampEs2 = page2.translations["es"]?.updatedAt!!
        val timestampEn2 = page2.translations["en"]?.updatedAt!!
        
        // 4. ASSERT: Timestamps exactamente iguales
        assertEquals(timestampEs1, timestampEs2, "Spanish timestamp should be preserved")
        assertEquals(timestampEn1, timestampEn2, "English timestamp should be preserved")
        
        // Verificar formato ISO 8601
        assertEquals("2026-02-16T11:00:00Z", timestampEs2)
        assertEquals("2026-02-16T11:05:00Z", timestampEn2)
    }
    
    @Test
    fun `portable persistence - modify html and reload`() {
        // 1. LOAD: Crear workspace inicial
        val workspace1 = createWorkspaceWithInfoPages()
        val projectId = workspace1.projects[0].id
        val pageId = workspace1.projects[0].tools.info?.pages?.get(0)?.id!!
        
        // 2. MODIFY: Actualizar HTML
        val result = infoUseCases.updatePage(
            workspace = workspace1,
            projectId = projectId,
            pageId = pageId,
            htmlEs = "<h1>Nuevo Título</h1><p>Contenido actualizado con <strong>negrita</strong> y <em>cursiva</em>.</p>"
        )
        
        assert(result.isSuccess)
        val workspace2 = result.getOrNull()!!
        
        // 3. SAVE: Serializar workspace modificado
        val jsonString = json.encodeToString(workspace2)
        
        // 4. RELOAD: Deserializar
        val workspace3 = json.decodeFromString<Workspace>(jsonString)
        val page3 = workspace3.projects[0].tools.info?.pages?.get(0)!!
        
        // 5. ASSERT: HTML modificado se preserva
        val htmlEs3 = page3.translations["es"]?.html!!
        
        assertEquals(
            "<h1>Nuevo Título</h1><p>Contenido actualizado con <strong>negrita</strong> y <em>cursiva</em>.</p>",
            htmlEs3,
            "Modified HTML should be preserved exactly"
        )
        
        // Verificar que inglés no cambió
        assertEquals(
            "<h1>Introduction</h1><p>Welcome to the project.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
            page3.translations["en"]?.html,
            "English HTML should remain unchanged"
        )
        
        // Verificar que el timestamp cambió solo en español
        val originalTimestamp = "2026-02-16T10:00:00Z"
        assert(page3.translations["es"]?.updatedAt != originalTimestamp) {
            "Spanish timestamp should have changed"
        }
    }
}

